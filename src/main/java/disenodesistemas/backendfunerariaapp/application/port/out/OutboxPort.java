package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.event.DomainEvent;

/**
 * Outbound port that durably records domain events for asynchronous downstream consumers
 * (ADR-0013). Application use cases call {@link #publish(DomainEvent)} immediately after the
 * business state transition has succeeded; the implementation guarantees the event row is
 * written in the caller's active transaction so the event becomes visible to the relay only
 * after the business commit.
 *
 * <p>The implementation is responsible for serialising the event to JSON, stamping a unique
 * idempotency key and resolving the active trace + correlation identifiers from the request
 * context — callers only provide the {@link DomainEvent} record itself.
 */
public interface OutboxPort {

  /**
   * Records the supplied event for later dispatch by the outbox relay. Must run inside the
   * caller's active {@link org.springframework.transaction.annotation.Transactional} so the
   * outbox row commits together with the business write; an event written outside a
   * transaction is a bug.
   *
   * @param event the domain event to publish; non-null
   */
  void publish(DomainEvent event);
}
