package disenodesistemas.backendfunerariaapp.application.usecase.audit;

import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import java.time.Instant;

/**
 * Optional criteria accepted by the audit log read API. Every field is nullable so callers can
 * combine any subset of filters (or none, returning the full audit trail page-by-page). The
 * record is used as a transport between {@code AuditEventController} and
 * {@code AuditEventQueryUseCase}; it deliberately stays in the application layer to keep
 * {@link AuditAction} as the single source of truth for valid actions.
 *
 * @param actorEmail exact match on {@code actor_email}
 * @param action exact match on the recorded {@link AuditAction}
 * @param targetType exact match on the aggregate type ({@code USER}, {@code FUNERAL}, ...)
 * @param targetId exact match on the target identifier (string, since DNIs and ids coexist)
 * @param from inclusive lower bound on {@code occurred_at}
 * @param to inclusive upper bound on {@code occurred_at}
 */
public record AuditEventFilter(
    String actorEmail,
    AuditAction action,
    String targetType,
    String targetId,
    Instant from,
    Instant to) {

  /** Convenience constant for callers that want to read the trail without any filter applied. */
  public static AuditEventFilter empty() {
    return new AuditEventFilter(null, null, null, null, null, null);
  }
}
