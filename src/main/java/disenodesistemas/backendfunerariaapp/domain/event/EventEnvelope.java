package disenodesistemas.backendfunerariaapp.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Metadata accompanying a {@link DomainEvent} dispatched through the outbox relay
 * (ADR-0014). Carries the fields a consumer needs to stitch the event back to the
 * originating HTTP request (trace + correlation), to ensure idempotency ({@code eventId})
 * and to display the event in chronological order ({@code occurredAt}).
 *
 * <p>Lives in the {@code domain.event} package alongside {@link DomainEvent} (not in
 * {@code application.port.out}) because it is a pure value object referenced by the
 * {@code DomainEventConsumer} port; keeping it here lets the {@code ports_must_be_interfaces}
 * ArchUnit rule stand without an exception.
 *
 * <p>The envelope is a record so it is immutable and trivially mockable in tests. Optional
 * trace fields are nullable: events recorded outside an HTTP request (scheduled jobs, future
 * inbound consumers) will not have a trace context to propagate.
 */
public record EventEnvelope(
    UUID eventId, Instant occurredAt, String traceId, String correlationId) {}
