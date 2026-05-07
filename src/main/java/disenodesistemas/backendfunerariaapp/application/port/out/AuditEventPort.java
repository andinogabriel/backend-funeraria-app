package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.AuditEvent;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Outbound port that records sensitive admin events into the audit log and exposes a paginated
 * read side for the admin query API. Application use cases call {@link #record} immediately after
 * the business state transition has succeeded, so the persisted entry reflects events that
 * actually happened. The read side is intentionally narrow: a single filtered, paginated search
 * is enough for the current compliance and forensic-review needs and avoids exposing the audit
 * table as a generic query surface.
 *
 * <p>The implementation is responsible for stamping the capture timestamp and for resolving the
 * active trace and correlation identifiers from the request context, so callers only need to
 * provide the business fields.
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

  /**
   * Returns audit entries matching the supplied criteria, sorted by capture time descending so
   * the most recent events come first. Every filter argument is optional and is applied as an
   * exact match (or an inclusive bound for the {@code occurredAt} window); passing all
   * {@code null} values yields the unfiltered audit trail. The supplied {@link Pageable} drives
   * slicing only — sorting is enforced server-side to keep the API deterministic for compliance
   * review.
   */
  Page<AuditEvent> search(
      String actorEmail,
      AuditAction action,
      String targetType,
      String targetId,
      Instant from,
      Instant to,
      Pageable pageable);
}
