package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.event.DomainEvent;
import disenodesistemas.backendfunerariaapp.domain.event.EventEnvelope;

/**
 * Outbound port representing a single subscriber on the domain-event stream (ADR-0014). The
 * {@link disenodesistemas.backendfunerariaapp.infrastructure.outbox.OutboxRelay relay} fans
 * each dequeued event out to every {@code DomainEventConsumer} bean registered in the Spring
 * context. Consumers run inside the relay's transaction; if a consumer throws, the relay
 * isolates the failure to that consumer (the outbox row still flips to PUBLISHED so other
 * consumers do not re-fire) and routes the offending pair (consumer, eventId) to a
 * consumer-level dead-letter sink.
 *
 * <h3>Idempotency contract</h3>
 *
 * Consumers must treat repeated calls with the same {@link EventEnvelope#eventId()} as
 * no-ops. The relay is at-least-once: a crash between {@code consume} returning and the row
 * being flipped to PUBLISHED will cause a redelivery on the next tick. Storage-backed
 * consumers typically rely on a unique constraint on {@code eventId} and silently swallow the
 * resulting integrity exception; broker-backed consumers use the broker's own dedup window.
 *
 * <h3>Why typed {@link DomainEvent} instead of the raw row</h3>
 *
 * The sealed {@code DomainEvent} hierarchy lets consumers use an exhaustive {@code switch} to
 * build their own projection without parsing JSON. The relay deserializes once per row and
 * passes the typed value to every consumer — duplicating the parse across consumers would be
 * wasteful, and exposing the JPA entity would couple consumers to a persistence shape they
 * have no business depending on.
 */
public interface DomainEventConsumer {

  /**
   * Short identifier used in logs and the consumer dead-letter table. Must be stable across
   * deployments so a backlog of dead-lettered rows can be re-driven to the same consumer.
   * Convention: kebab-case, e.g. {@code "activity-log"}.
   */
  String name();

  /**
   * Handles a single dequeued event. Implementations must be idempotent on
   * {@link EventEnvelope#eventId()} — see the class-level contract.
   *
   * @param event the deserialized domain event
   * @param envelope the relay-supplied metadata (eventId, trace context, occurredAt)
   */
  void consume(DomainEvent event, EventEnvelope envelope);
}
