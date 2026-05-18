package disenodesistemas.backendfunerariaapp.infrastructure.outbox;

import disenodesistemas.backendfunerariaapp.application.port.out.DomainEventConsumer;
import disenodesistemas.backendfunerariaapp.domain.event.EventEnvelope;
import disenodesistemas.backendfunerariaapp.domain.entity.OutboxEvent;
import disenodesistemas.backendfunerariaapp.domain.enums.OutboxStatus;
import disenodesistemas.backendfunerariaapp.domain.event.DomainEvent;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.OutboxEventRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Background poller that drains the outbox table (ADR-0013) and fans every dequeued event out
 * to every registered {@link DomainEventConsumer} (ADR-0014). Every tick it loads the next
 * batch of {@link OutboxStatus#PENDING} rows, deserialises each payload into the typed
 * {@link DomainEvent}, invokes each consumer once and flips the row to
 * {@link OutboxStatus#PUBLISHED}.
 *
 * <h3>Scheduling</h3>
 *
 * Runs on a fixed delay (default 5 s) measured from the previous run's completion so two
 * ticks cannot overlap inside the same JVM. The {@code app.outbox.poll-interval-ms} property
 * lets deployments tune the cadence; {@code app.outbox.batch-size} caps the per-tick row
 * count so a sudden backlog cannot starve the database connection pool.
 *
 * <h3>Delivery semantics</h3>
 *
 * At-least-once at the outbox layer (a crash between consumer invocation and the row's status
 * flip will redeliver on the next tick) and at-least-once per consumer independently. The row
 * flips to PUBLISHED after the fan-out regardless of which consumers failed — re-firing
 * already-succeeded consumers on retry would violate idempotency expectations of consumers
 * that talk to non-idempotent downstreams. Per-consumer failures are logged with the
 * {@code outbox.consumer.failed} event so an operator alert can dead-letter them out of band;
 * a future PR adds a {@code consumer_dead_letters} table and a re-drive job.
 *
 * <h3>Poison pills</h3>
 *
 * A payload that cannot be deserialised into a known {@link DomainEvent} subtype is
 * non-retryable — looping the row through the relay forever would just consume a database
 * slot. Such rows are marked {@link OutboxStatus#FAILED} immediately so they stop being
 * picked up; the offending payload is left intact for forensic inspection.
 *
 * <h3>Concurrency &amp; idempotency</h3>
 *
 * Single-instance for now — the deploy runs one container so there is no risk of two relays
 * racing on the same row. Multi-instance deployments will need either Shedlock or a
 * {@code SELECT … FOR UPDATE SKIP LOCKED} fetch; both options are documented in ADR-0013 and
 * will be added when the deploy topology changes. The unique {@code event_id} column on
 * every row guarantees downstream idempotency even if a row is replayed after a crash.
 */
@Component
@Slf4j
public class OutboxRelay {

  private final OutboxEventRepository repository;
  private final DomainEventDeserializer deserializer;
  private final List<DomainEventConsumer> consumers;
  private final Clock clock;
  private final int batchSize;

  /**
   * Production-time constructor wired by Spring. Defaults the clock to {@link Clock#systemUTC()}
   * so the relay behaves correctly without an extra {@code @Bean} declaration; tests use the
   * package-private overload below to inject a deterministic clock. Spring injects every
   * {@link DomainEventConsumer} bean in the context into the {@code consumers} list — adding
   * a new consumer is therefore a zero-config change.
   */
  @Autowired
  public OutboxRelay(
      final OutboxEventRepository repository,
      final DomainEventDeserializer deserializer,
      final List<DomainEventConsumer> consumers,
      @Value("${app.outbox.batch-size:100}") final int batchSize) {
    this(repository, deserializer, consumers, batchSize, Clock.systemUTC());
  }

  /** Test-friendly overload that lets a deterministic clock drive {@code publishedAt}. */
  public OutboxRelay(
      final OutboxEventRepository repository,
      final DomainEventDeserializer deserializer,
      final List<DomainEventConsumer> consumers,
      final int batchSize,
      final Clock clock) {
    this.repository = repository;
    this.deserializer = deserializer;
    this.consumers = consumers;
    this.batchSize = batchSize;
    this.clock = clock;
  }

  /**
   * Loads the next batch of {@link OutboxStatus#PENDING} rows and fans each one out to every
   * registered consumer. The whole batch runs inside a single transaction so the row status
   * updates commit atomically with the consumer's writes (the activity-log consumer relies on
   * this — its inserts are part of the same JPA persistence context).
   */
  @Scheduled(
      fixedDelayString = "${app.outbox.poll-interval-ms:5000}",
      initialDelayString = "${app.outbox.initial-delay-ms:10000}")
  @Transactional
  public void drain() {
    final List<OutboxEvent> batch =
        repository.findNextBatch(OutboxStatus.PENDING, PageRequest.of(0, batchSize));
    if (batch.isEmpty()) {
      return;
    }

    final Instant now = Instant.now(clock);
    int published = 0;
    int failed = 0;
    int consumerFailures = 0;
    for (final OutboxEvent row : batch) {
      final DomainEvent event;
      try {
        event = deserializer.deserialize(row);
      } catch (final DomainEventDeserializationException poisonPill) {
        // Non-retryable: the payload shape is broken, no number of replays will help. Stop
        // the row from looping by moving it straight to terminal FAILED.
        row.markExhausted(poisonPill.getMessage());
        failed++;
        log.atError()
            .setCause(poisonPill)
            .addKeyValue("event", "outbox.event.poison_pill")
            .addKeyValue("eventId", row.getEventId())
            .log("outbox.event.poison_pill");
        continue;
      }

      final EventEnvelope envelope =
          new EventEnvelope(
              row.getEventId(), row.getOccurredAt(), row.getTraceId(), row.getCorrelationId());
      consumerFailures += fanOut(event, envelope, row);
      row.markPublished(now);
      published++;
    }

    log.atInfo()
        .addKeyValue("event", "outbox.batch.completed")
        .addKeyValue("size", batch.size())
        .addKeyValue("published", published)
        .addKeyValue("failed", failed)
        .addKeyValue("consumerFailures", consumerFailures)
        .log("outbox.batch.completed");
  }

  /**
   * Dispatches a single event to every registered consumer. Per-consumer failures are
   * isolated so one misbehaving consumer cannot block the rest of the fan-out. Returns the
   * count of consumers that threw — used only for the batch log line.
   */
  private int fanOut(
      final DomainEvent event, final EventEnvelope envelope, final OutboxEvent row) {
    int failures = 0;
    for (final DomainEventConsumer consumer : consumers) {
      try {
        consumer.consume(event, envelope);
      } catch (final RuntimeException ex) {
        failures++;
        log.atWarn()
            .setCause(ex)
            .addKeyValue("event", "outbox.consumer.failed")
            .addKeyValue("eventId", row.getEventId())
            .addKeyValue("eventType", row.getEventType())
            .addKeyValue("consumer", consumer.name())
            .log("outbox.consumer.failed");
      }
    }
    return failures;
  }
}
