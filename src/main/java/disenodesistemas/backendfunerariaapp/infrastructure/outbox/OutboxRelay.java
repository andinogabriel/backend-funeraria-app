package disenodesistemas.backendfunerariaapp.infrastructure.outbox;

import disenodesistemas.backendfunerariaapp.domain.entity.OutboxEvent;
import disenodesistemas.backendfunerariaapp.domain.enums.OutboxStatus;
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
 * Background poller that drains the outbox table (ADR-0013). Every tick it loads the next
 * batch of {@link OutboxStatus#PENDING} rows, dispatches each event downstream and flips the
 * row to {@link OutboxStatus#PUBLISHED}. In v1 there is no concrete consumer yet — "dispatch"
 * is a structured log line at INFO carrying the full event payload, so a log-aware sink can
 * already consume the event stream while the broker integration is being designed.
 *
 * <h3>Scheduling</h3>
 *
 * Runs on a fixed delay (default 5 s) measured from the previous run's completion so two
 * ticks cannot overlap inside the same JVM. The {@code app.outbox.poll-interval-ms} property
 * lets deployments tune the cadence; {@code app.outbox.batch-size} caps the per-tick row
 * count so a sudden backlog cannot starve the database connection pool.
 *
 * <h3>Concurrency &amp; idempotency</h3>
 *
 * Single-instance for now — the deploy runs one container so there is no risk of two relays
 * racing on the same row. Multi-instance deployments will need either Shedlock or a
 * {@code SELECT … FOR UPDATE SKIP LOCKED} fetch; both options are documented in ADR-0013 and
 * will be added when the deploy topology changes. The unique {@code event_id} column on
 * every row guarantees downstream idempotency even if a row is replayed after a crash.
 *
 * <h3>Retries</h3>
 *
 * Dispatch failures bump the row's {@code attempts} counter and leave it in
 * {@link OutboxStatus#PENDING} so the next tick retries. Once {@link OutboxEvent#MAX_ATTEMPTS}
 * is reached the row moves to terminal {@link OutboxStatus#FAILED} and stops being picked up;
 * an operator alert wired off the {@code outbox.batch.failed} log line is the follow-up.
 */
@Component
@Slf4j
public class OutboxRelay {

  private final OutboxEventRepository repository;
  private final Clock clock;
  private final int batchSize;

  /**
   * Production-time constructor wired by Spring. Defaults the clock to {@link Clock#systemUTC()}
   * so the relay behaves correctly without an extra {@code @Bean} declaration; tests use the
   * package-private overload below to inject a deterministic clock.
   */
  @Autowired
  public OutboxRelay(
      final OutboxEventRepository repository,
      @Value("${app.outbox.batch-size:100}") final int batchSize) {
    this(repository, batchSize, Clock.systemUTC());
  }

  /** Test-friendly overload that lets a deterministic clock drive {@code publishedAt}. */
  public OutboxRelay(
      final OutboxEventRepository repository, final int batchSize, final Clock clock) {
    this.repository = repository;
    this.batchSize = batchSize;
    this.clock = clock;
  }

  /**
   * Loads the next batch of {@link OutboxStatus#PENDING} rows and dispatches each one. The
   * whole batch runs inside a single transaction so a downstream failure does not leak
   * partial state — the offending row's status update rolls back together with the rest of
   * the batch, the next tick retries.
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
    for (final OutboxEvent row : batch) {
      try {
        dispatch(row);
        row.markPublished(now);
        published++;
      } catch (final RuntimeException ex) {
        // Per-row failures are isolated: bump the row's attempt counter and leave the rest
        // of the batch to keep flowing. The `markFailure` call mutates the managed entity so
        // the next-tick read sees the updated counter even without an explicit save call.
        if (row.getAttempts() + 1 >= OutboxEvent.MAX_ATTEMPTS) {
          row.markExhausted(ex.getMessage());
        } else {
          row.markFailure(ex.getMessage());
        }
        failed++;
        log.atWarn()
            .setCause(ex)
            .addKeyValue("event", "outbox.event.dispatch_failed")
            .addKeyValue("eventId", row.getEventId())
            .addKeyValue("attempts", row.getAttempts())
            .log("outbox.event.dispatch_failed");
      }
    }

    log.atInfo()
        .addKeyValue("event", "outbox.batch.completed")
        .addKeyValue("size", batch.size())
        .addKeyValue("published", published)
        .addKeyValue("failed", failed)
        .log("outbox.batch.completed");
  }

  /**
   * Hook where the future broker integration will plug in. The v1 implementation emits a
   * structured log line carrying every field a consumer would otherwise read from the row —
   * eventType, aggregate, payload, trace context — so the log pipeline already carries the
   * event stream. Replacing the log line with a real publish call is a one-method change.
   */
  private void dispatch(final OutboxEvent row) {
    log.atInfo()
        .addKeyValue("event", "outbox.event.published")
        .addKeyValue("eventId", row.getEventId())
        .addKeyValue("eventType", row.getEventType())
        .addKeyValue("aggregateType", row.getAggregateType())
        .addKeyValue("aggregateId", row.getAggregateId())
        .addKeyValue("payload", row.getPayload())
        .addKeyValue("traceId", row.getTraceId())
        .addKeyValue("correlationId", row.getCorrelationId())
        .log("outbox.event.published");
  }
}
