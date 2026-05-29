package disenodesistemas.backendfunerariaapp.domain.entity;

import disenodesistemas.backendfunerariaapp.domain.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Persisted notification visible from the in-app alert center. Backed by the
 * {@code notifications} table created in V15.
 *
 * <p>The payload is modelled as a {@code String} carrying serialised JSON — same pattern
 * the {@link OutboxEvent} table uses (we did not introduce a Hibernate JSONB type
 * dependency for this single column). The DB-side type is JSONB so PostgreSQL still
 * validates the JSON on insert and the future indexable query paths stay available.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Outbox event id that produced this row — used as the idempotency key so a relay
   * redelivery is rejected by the {@code UNIQUE} constraint rather than producing a
   * duplicate alert. Same pattern the {@link ActivityLogEntry} table already uses.
   */
  @Column(name = "event_id", nullable = false, unique = true)
  private UUID eventId;

  /**
   * Free-text descriptor of who the alert is for. v1 ships only {@code "ROLE_ADMIN"}
   * (broadcast); future shapes can use {@code "USER:<id>"} for direct messages without a
   * schema change.
   */
  @Column(nullable = false, length = 50)
  private String audience;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private NotificationType type;

  /**
   * Type-specific JSON payload as a string. Stored as {@code text} so Hibernate binds the
   * value as a plain string — same pattern the {@link OutboxEvent} payload uses; readers
   * deserialise with Jackson on consumption.
   */
  @Column(nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  /**
   * Set the moment any consumer of the {@code audience} marks the row as read. Until
   * then the bell icon counts the row as unread. Single timestamp on purpose — see V15
   * comment for the audience-vs-per-user trade-off.
   */
  @Column(name = "read_at")
  private Instant readAt;
}
