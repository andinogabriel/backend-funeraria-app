package disenodesistemas.backendfunerariaapp.domain.enums;

/**
 * Lifecycle status of an outbox row (ADR-0013).
 *
 * <ul>
 *   <li>{@link #PENDING} — written by the use case, not yet shipped to consumers.
 *   <li>{@link #PUBLISHED} — the relay successfully dispatched the event downstream.
 *   <li>{@link #FAILED} — terminal state after the relay exhausted its retry budget. Rows in
 *       this state require operator review; the relay does not pick them up again.
 * </ul>
 */
public enum OutboxStatus {
  PENDING,
  PUBLISHED,
  FAILED
}
