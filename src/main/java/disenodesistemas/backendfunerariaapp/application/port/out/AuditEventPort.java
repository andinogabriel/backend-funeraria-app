package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;

/**
 * Outbound port that records sensitive admin events into the audit log. Application use cases
 * call {@link #record} immediately after the business state transition has succeeded, so the
 * persisted entry reflects events that actually happened. The implementation is responsible for
 * stamping the capture timestamp and for resolving the active trace and correlation identifiers
 * from the request context, so callers only need to provide the business fields.
 */
public interface AuditEventPort {

  /**
   * Persists an audit entry describing a single business event.
   *
   * @param action the catalog entry that classifies the event for reporting
   * @param actorEmail email of the user whose action triggered the event
   * @param actorId optional identifier of the acting user when one is known; {@code null} for
   *     system-driven flows where no user owns the action
   * @param targetType aggregate type the event applies to ({@code USER}, {@code FUNERAL}, ...)
   * @param targetId stable identifier of the target aggregate as a string
   * @param payload optional free-form detail (typically a small JSON document or a brief
   *     human-readable summary) preserved verbatim for forensic review; {@code null} when the
   *     action and target are self-explanatory
   */
  void record(
      AuditAction action,
      String actorEmail,
      Long actorId,
      String targetType,
      String targetId,
      String payload);
}
