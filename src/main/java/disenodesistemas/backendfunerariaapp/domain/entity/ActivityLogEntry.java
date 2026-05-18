package disenodesistemas.backendfunerariaapp.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Read-model row that powers the dashboard's activity feed (ADR-0014). One row per domain
 * event consumed by
 * {@link disenodesistemas.backendfunerariaapp.infrastructure.outbox.consumer.ActivityLogConsumer}.
 *
 * <p>Kept in the {@code domain.entity} package alongside {@link OutboxEvent} for consistency
 * with the rest of the schema even though strictly the activity log is a CQRS read model. The
 * tradeoff: a single JPA mapping location is easier to navigate than scattering entities by
 * write-side / read-side ownership.
 *
 * <h3>Idempotency</h3>
 *
 * The {@code event_id} column carries a unique constraint so a redelivery from the outbox
 * relay (at-least-once semantics) inserts at most once. The consumer catches the resulting
 * integrity violation and logs at debug — duplicate consumption is the expected steady-state
 * behaviour after a relay crash recovery.
 */
@Entity
@Table(name = "activity_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityLogEntry {

  /**
   * Maximum length of {@link #summary}. Matches the column definition so a use case that
   * accidentally over-runs the limit fails with a clear domain error instead of a Postgres
   * truncation surprise.
   */
  public static final int SUMMARY_MAX_LENGTH = 512;

  @Id @GeneratedValue private Long id;

  /** Idempotency key — same UUID the outbox row carried. Unique across the table. */
  @Column(name = "event_id", nullable = false, updatable = false)
  private UUID eventId;

  @Column(name = "event_type", nullable = false, updatable = false, length = 64)
  private String eventType;

  @Column(name = "aggregate_type", nullable = false, updatable = false, length = 64)
  private String aggregateType;

  @Column(name = "aggregate_id", nullable = false, updatable = false, length = 128)
  private String aggregateId;

  /** Human-readable summary built from the typed event via an exhaustive switch. */
  @Column(name = "summary", nullable = false, updatable = false, length = SUMMARY_MAX_LENGTH)
  private String summary;

  @Column(name = "occurred_at", nullable = false, updatable = false)
  private Instant occurredAt;

  @Column(name = "trace_id", length = 128)
  private String traceId;

  /**
   * Builds a fresh activity-log row. Used by {@code ActivityLogConsumer} only — there is no
   * other writer in the system.
   */
  public ActivityLogEntry(
      final UUID eventId,
      final String eventType,
      final String aggregateType,
      final String aggregateId,
      final String summary,
      final Instant occurredAt,
      final String traceId) {
    this.eventId = eventId;
    this.eventType = eventType;
    this.aggregateType = aggregateType;
    this.aggregateId = aggregateId;
    this.summary = summary;
    this.occurredAt = occurredAt;
    this.traceId = traceId;
  }
}
