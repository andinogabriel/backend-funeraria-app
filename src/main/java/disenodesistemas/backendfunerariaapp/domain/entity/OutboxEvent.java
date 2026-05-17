package disenodesistemas.backendfunerariaapp.domain.entity;

import disenodesistemas.backendfunerariaapp.domain.enums.OutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Persistent outbox row backing {@code OutboxPort.publish} (ADR-0013). Lifecycle:
 *
 * <ol>
 *   <li>Use case calls {@code publish} inside its own {@code @Transactional} — adapter inserts
 *       a row with {@code status = PENDING}.
 *   <li>{@code OutboxRelay} picks the row up on its next tick, ships the event downstream and
 *       calls {@link #markPublished(Instant)} which flips the status + stamps the publish time.
 *   <li>On a publish failure the relay calls {@link #markFailure(String)} which bumps the
 *       attempt counter; after the retry budget is exhausted the relay calls
 *       {@link #markExhausted(String)} to move the row to terminal {@code FAILED}.
 * </ol>
 *
 * <p>The entity stays in the {@code domain} package because the lifecycle methods encode
 * business rules around retries; the infrastructure layer treats them as opaque transitions.
 */
@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

  /** Maximum retries the relay attempts before moving a row to {@link OutboxStatus#FAILED}. */
  public static final int MAX_ATTEMPTS = 5;

  @Id @GeneratedValue private Long id;

  /** Consumer-facing idempotency key. Unique across the table. */
  @Column(name = "event_id", nullable = false, updatable = false)
  private UUID eventId;

  @Column(name = "event_type", nullable = false, updatable = false, length = 64)
  private String eventType;

  @Column(name = "aggregate_type", nullable = false, updatable = false, length = 64)
  private String aggregateType;

  @Column(name = "aggregate_id", nullable = false, updatable = false, length = 128)
  private String aggregateId;

  /** Jackson-serialized {@code DomainEvent} subclass; carries a {@code @type} discriminator. */
  @Column(name = "payload", nullable = false, updatable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(name = "occurred_at", nullable = false, updatable = false)
  private Instant occurredAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 16)
  private OutboxStatus status;

  @Column(name = "published_at")
  private Instant publishedAt;

  @Column(name = "attempts", nullable = false)
  private Integer attempts;

  @Column(name = "last_error", columnDefinition = "TEXT")
  private String lastError;

  @Column(name = "trace_id", length = 128)
  private String traceId;

  @Column(name = "correlation_id", length = 128)
  private String correlationId;

  /**
   * Constructs a fresh {@code PENDING} row. Used by {@code JpaOutboxAdapter} only — callers in
   * the application layer go through {@code OutboxPort.publish} so trace metadata is resolved
   * consistently.
   */
  public OutboxEvent(
      final UUID eventId,
      final String eventType,
      final String aggregateType,
      final String aggregateId,
      final String payload,
      final Instant occurredAt,
      final String traceId,
      final String correlationId) {
    this.eventId = eventId;
    this.eventType = eventType;
    this.aggregateType = aggregateType;
    this.aggregateId = aggregateId;
    this.payload = payload;
    this.occurredAt = occurredAt;
    this.status = OutboxStatus.PENDING;
    this.attempts = 0;
    this.traceId = traceId;
    this.correlationId = correlationId;
  }

  /** Marks the row as successfully shipped downstream. */
  public void markPublished(final Instant publishedAt) {
    this.status = OutboxStatus.PUBLISHED;
    this.publishedAt = publishedAt;
    this.attempts = this.attempts + 1;
    this.lastError = null;
  }

  /**
   * Records a failed dispatch attempt. Keeps the row in {@link OutboxStatus#PENDING} so the
   * relay will retry on its next tick; the caller is responsible for noticing
   * {@link #attempts} reached {@link #MAX_ATTEMPTS} and calling {@link #markExhausted(String)}.
   */
  public void markFailure(final String error) {
    this.attempts = this.attempts + 1;
    this.lastError = error;
  }

  /** Terminal transition into {@link OutboxStatus#FAILED}; relay stops retrying. */
  public void markExhausted(final String error) {
    this.status = OutboxStatus.FAILED;
    this.lastError = error;
  }
}
