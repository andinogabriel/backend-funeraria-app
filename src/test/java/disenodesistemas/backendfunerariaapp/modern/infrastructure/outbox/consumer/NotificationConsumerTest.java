package disenodesistemas.backendfunerariaapp.modern.infrastructure.outbox.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.NotificationPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.NotificationEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.NotificationType;
import disenodesistemas.backendfunerariaapp.domain.event.AffiliateDeleted;
import disenodesistemas.backendfunerariaapp.domain.event.EventEnvelope;
import disenodesistemas.backendfunerariaapp.domain.event.LowStockReached;
import disenodesistemas.backendfunerariaapp.infrastructure.outbox.consumer.NotificationConsumer;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("NotificationConsumer")
class NotificationConsumerTest {

  @Mock private NotificationPersistencePort port;

  @InjectMocks private NotificationConsumer consumer;

  @Test
  @DisplayName(
      "Given a LowStockReached event when consume runs then it persists a notifications row scoped to ROLE_ADMIN with the JSON payload mirrored verbatim")
  void persistsLowStockReachedEvent() {
    final UUID eventId = UUID.randomUUID();
    final Instant occurredAt = Instant.parse("2026-05-28T18:00:00Z");
    final var envelope = new EventEnvelope(eventId, occurredAt, "trace-1", "corr-1");
    final var event = new LowStockReached(42L, "ATAUD-01", "Ataud premium", 10, 11, 8);
    when(port.existsByEventId(eventId)).thenReturn(false);

    consumer.consume(event, envelope);

    final ArgumentCaptor<NotificationEntity> captor =
        ArgumentCaptor.forClass(NotificationEntity.class);
    verify(port).save(captor.capture());
    final NotificationEntity persisted = captor.getValue();
    assertThat(persisted.getEventId()).isEqualTo(eventId);
    assertThat(persisted.getAudience()).isEqualTo(NotificationConsumer.AUDIENCE_ROLE_ADMIN);
    assertThat(persisted.getType()).isEqualTo(NotificationType.LOW_STOCK_REACHED);
    assertThat(persisted.getCreatedAt()).isEqualTo(occurredAt);
    assertThat(persisted.getReadAt()).isNull();
    assertThat(persisted.getPayload())
        .isEqualTo(
            "{\"itemId\":42,\"code\":\"ATAUD-01\",\"name\":\"Ataud premium\","
                + "\"threshold\":10,\"stockBefore\":11,\"stockAfter\":8}");
  }

  @Test
  @DisplayName(
      "Given an event that already produced a row (idempotency check) when consume runs then it skips the insert silently — relay redeliveries are no-ops")
  void duplicateEventIdSkipsSilently() {
    final UUID eventId = UUID.randomUUID();
    final var envelope = new EventEnvelope(eventId, Instant.now(), "trace-1", "corr-1");
    final var event = new LowStockReached(42L, "ATAUD-01", "Ataud", 10, 11, 8);
    when(port.existsByEventId(eventId)).thenReturn(true);

    consumer.consume(event, envelope);

    verify(port, never()).save(any());
  }

  @Test
  @DisplayName(
      "Given a non-LowStockReached event when consume runs then it ignores it entirely — the notifications surface is type-scoped, the activity log handles the rest")
  void otherEventTypesAreIgnored() {
    final var envelope = new EventEnvelope(UUID.randomUUID(), Instant.now(), "trace-1", "corr-1");
    final var unrelated = new AffiliateDeleted(30111222);

    consumer.consume(unrelated, envelope);

    verify(port, never()).existsByEventId(any());
    verify(port, never()).save(any());
  }
}
