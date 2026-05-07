package disenodesistemas.backendfunerariaapp.domain.entity;

import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Immutable audit-trail entry capturing a single sensitive admin event. Once persisted, an
 * {@code AuditEvent} is never updated: corrections are recorded as new entries so the history
 * can be replayed deterministically. Every field except {@code actorId}, {@code traceId},
 * {@code correlationId} and {@code payload} is required by the schema.
 */
@Entity
@Table(name = "audit_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditEvent {

  @Id
  @GeneratedValue
  private Long id;

  /** UTC instant when the event was captured by the application. */
  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  /** Email of the user who triggered the event, as known to the security context. */
  @Column(name = "actor_email", nullable = false, length = 255)
  private String actorEmail;

  /** Identifier of the acting user when one is available; {@code null} for system-driven events. */
  @Column(name = "actor_id")
  private Long actorId;

  @Enumerated(EnumType.STRING)
  @Column(name = "action", nullable = false, length = 64)
  private AuditAction action;

  /** Aggregate type the event applies to ({@code USER}, {@code FUNERAL}, {@code AFFILIATE}, ...). */
  @Column(name = "target_type", nullable = false, length = 64)
  private String targetType;

  /** Stable identifier of the target aggregate as a string so it works for numeric and DNI ids. */
  @Column(name = "target_id", nullable = false, length = 128)
  private String targetId;

  /** Trace identifier from the active OpenTelemetry span at capture time, when available. */
  @Column(name = "trace_id", length = 128)
  private String traceId;

  /** Optional client-supplied correlation identifier propagated through the request context. */
  @Column(name = "correlation_id", length = 128)
  private String correlationId;

  /** Free-form payload (currently plain text or JSON-as-string) for forensic detail. */
  @Column(name = "payload", columnDefinition = "TEXT")
  private String payload;

  /**
   * Constructs a fully-populated audit entry. Marked as the canonical constructor used by the
   * persistence adapter; callers from the application layer go through {@code AuditEventPort}
   * instead so trace metadata is resolved consistently.
   */
  public AuditEvent(
      final Instant occurredAt,
      final String actorEmail,
      final Long actorId,
      final AuditAction action,
      final String targetType,
      final String targetId,
      final String traceId,
      final String correlationId,
      final String payload) {
    this.occurredAt = occurredAt;
    this.actorEmail = actorEmail;
    this.actorId = actorId;
    this.action = action;
    this.targetType = targetType;
    this.targetId = targetId;
    this.traceId = traceId;
    this.correlationId = correlationId;
    this.payload = payload;
  }
}
