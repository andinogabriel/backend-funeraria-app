package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import disenodesistemas.backendfunerariaapp.application.port.out.OutboxPort;
import disenodesistemas.backendfunerariaapp.domain.entity.OutboxEvent;
import disenodesistemas.backendfunerariaapp.domain.event.DomainEvent;
import disenodesistemas.backendfunerariaapp.infrastructure.logging.RequestTraceContext;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.OutboxEventRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA-backed {@link OutboxPort} implementation (ADR-0013). The {@code publish} call inserts a
 * {@code PENDING} row in the caller's active transaction so the event commits together with
 * the business write — propagation is {@code MANDATORY} to fail loudly if a use case forgets
 * to wrap the call in a {@link Transactional} boundary (writing the outbox row in its own
 * transaction would defeat the pattern by allowing the business write to roll back without
 * dropping the event).
 *
 * <p>Trace and correlation identifiers are resolved from {@link RequestTraceContext} so
 * downstream consumers can stitch the published event back to the originating HTTP request.
 */
@Component
@Slf4j
public class JpaOutboxAdapter implements OutboxPort {

  private final OutboxEventRepository repository;
  private final ObjectMapper objectMapper;
  private final Clock clock;

  /** Production-time constructor wired by Spring; defaults the clock to {@link Clock#systemUTC()}. */
  @Autowired
  public JpaOutboxAdapter(
      final OutboxEventRepository repository, final ObjectMapper objectMapper) {
    this(repository, objectMapper, Clock.systemUTC());
  }

  /** Test-friendly overload that lets a deterministic clock drive the {@code occurredAt} stamp. */
  public JpaOutboxAdapter(
      final OutboxEventRepository repository,
      final ObjectMapper objectMapper,
      final Clock clock) {
    this.repository = repository;
    this.objectMapper = objectMapper;
    this.clock = clock;
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void publish(final DomainEvent event) {
    final OutboxEvent row =
        new OutboxEvent(
            UUID.randomUUID(),
            event.eventType(),
            event.aggregateType(),
            event.aggregateId(),
            serializePayload(event),
            Instant.now(clock),
            RequestTraceContext.currentTraceId(),
            RequestTraceContext.currentCorrelationId());
    repository.save(row);

    log.atInfo()
        .addKeyValue("event", "outbox.event.recorded")
        .addKeyValue("eventType", event.eventType())
        .addKeyValue("aggregateType", event.aggregateType())
        .addKeyValue("aggregateId", event.aggregateId())
        .addKeyValue("eventId", row.getEventId())
        .log("outbox.event.recorded");
  }

  private String serializePayload(final DomainEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (final JsonProcessingException e) {
      // A Jackson failure here means the event record's shape is non-serializable — that is a
      // programming error, not a runtime condition we want to recover from. Throwing an
      // unchecked exception inside @Transactional rolls back the business write too, which is
      // exactly the right thing to do because shipping a half-recorded event would silently
      // drop the publish.
      throw new IllegalStateException(
          "Failed to serialize domain event " + event.eventType(), e);
    }
  }
}
