package disenodesistemas.backendfunerariaapp.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Stored outcome of a mutating request identified by (principal, Idempotency-Key). See V17 for
 * the IN_PROGRESS -> COMPLETED lifecycle; a row that would end in a 5xx is deleted instead.
 */
@Entity
@Table(name = "idempotency_keys")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdempotencyRecord {

  /** Lifecycle of a stored key. */
  public enum Status {
    IN_PROGRESS,
    COMPLETED
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String principal;

  @Column(name = "idempotency_key", nullable = false, length = 100)
  private String idempotencyKey;

  @Column(nullable = false, length = 10)
  private String method;

  @Column(nullable = false, length = 500)
  private String path;

  @Column(name = "request_hash", nullable = false, length = 64)
  private String requestHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status;

  @Column(name = "response_status")
  private Integer responseStatus;

  @Column(name = "response_content_type")
  private String responseContentType;

  @Column(name = "response_body", columnDefinition = "text")
  private String responseBody;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  /** New IN_PROGRESS reservation for a request that is about to run. */
  public static IdempotencyRecord reserve(
      final String principal,
      final String idempotencyKey,
      final String method,
      final String path,
      final String requestHash,
      final Instant now,
      final Instant expiresAt) {
    final IdempotencyRecord record = new IdempotencyRecord();
    record.principal = principal;
    record.idempotencyKey = idempotencyKey;
    record.method = method;
    record.path = path;
    record.requestHash = requestHash;
    record.status = Status.IN_PROGRESS;
    record.createdAt = now;
    record.expiresAt = expiresAt;
    return record;
  }

  /** Stores the response that retries with the same key will receive. */
  public void complete(final int status, final String contentType, final String body) {
    this.status = Status.COMPLETED;
    this.responseStatus = status;
    this.responseContentType = contentType;
    this.responseBody = body;
  }

  public boolean isExpired(final Instant now) {
    return !expiresAt.isAfter(now);
  }
}
