package disenodesistemas.backendfunerariaapp.modern.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.domain.event.EventEnvelope;
import disenodesistemas.backendfunerariaapp.application.port.out.OutboxPort;
import disenodesistemas.backendfunerariaapp.application.usecase.metrics.ActivityFeedQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.event.AffiliateDeleted;
import disenodesistemas.backendfunerariaapp.domain.event.FuneralCreated;
import disenodesistemas.backendfunerariaapp.infrastructure.outbox.OutboxRelay;
import disenodesistemas.backendfunerariaapp.infrastructure.outbox.consumer.ActivityLogConsumer;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.ActivityLogRepository;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.OutboxEventRepository;
import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
import disenodesistemas.backendfunerariaapp.web.dto.response.ActivityFeedResponseDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * End-to-end smoke for the outbox consumer + activity-feed read model (ADR-0014). Publishes
 * events through the outbox, drives the relay manually (so we are not racing the
 * {@code @Scheduled} cadence) and verifies the activity_log row materialises with the
 * expected summary, ready for the dashboard endpoint to read.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ActivityLogConsumerPostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  @Autowired private OutboxPort outboxPort;
  @Autowired private OutboxRelay outboxRelay;
  @Autowired private OutboxEventRepository outboxRepository;
  @Autowired private ActivityLogRepository activityLogRepository;
  @Autowired private ActivityFeedQueryUseCase activityFeedQueryUseCase;
  @Autowired private ActivityLogConsumer activityLogConsumer;
  @Autowired private TransactionTemplate transactionTemplate;

  @AfterEach
  void clearStateBetweenTests() {
    activityLogRepository.deleteAll();
    outboxRepository.deleteAll();
  }

  @Test
  @DisplayName(
      "Given a published FuneralCreated event when the relay drains then an activity_log row is materialised with the Spanish summary")
  void shouldMaterialiseActivityRowForFuneralCreated() {
    transactionTemplate.executeWithoutResult(
        status ->
            outboxPort.publish(
                new FuneralCreated(
                    42L,
                    "REC-001",
                    "0001",
                    new BigDecimal("250000"),
                    7L,
                    30111222,
                    "Juan Pérez")));

    outboxRelay.drain();

    assertThat(activityLogRepository.findAll())
        .singleElement()
        .satisfies(
            row -> {
              assertThat(row.getEventType()).isEqualTo("FUNERAL_CREATED");
              assertThat(row.getAggregateType()).isEqualTo("FUNERAL");
              assertThat(row.getAggregateId()).isEqualTo("42");
              assertThat(row.getSummary())
                  .isEqualTo(
                      "Nuevo servicio registrado: recibo REC-001 para Juan Pérez (total $250000)");
            });
  }

  @Test
  @DisplayName(
      "Given multiple events when the activity feed is queried then the entries come back newest-first")
  void feedReturnsEntriesNewestFirst() {
    transactionTemplate.executeWithoutResult(
        status -> {
          outboxPort.publish(new AffiliateDeleted(11111111));
          outboxPort.publish(new AffiliateDeleted(22222222));
        });

    outboxRelay.drain();

    final ActivityFeedResponseDto response = activityFeedQueryUseCase.getRecentActivity(10);
    assertThat(response.entries()).hasSize(2);
    // Both rows share the relay's batch timestamp; ordering is then by id desc (latest insert
    // first). The second publish wins.
    assertThat(response.entries().get(0).aggregateId()).isEqualTo("22222222");
    assertThat(response.entries().get(1).aggregateId()).isEqualTo("11111111");
  }

  @Test
  @DisplayName(
      "Given the consumer is invoked twice with the same eventId then only one activity_log row exists")
  void duplicateDeliveryIsIdempotent() {
    final UUID eventId = UUID.randomUUID();
    final EventEnvelope envelope =
        new EventEnvelope(eventId, Instant.parse("2026-05-17T12:00:00Z"), "trace", "corr");
    final AffiliateDeleted event = new AffiliateDeleted(33333333);

    // First call inserts the row. Run inside a TransactionTemplate so the JPA persistence
    // context is flushed before the second call (the unique constraint check happens at
    // commit-time on the deferred insert otherwise).
    transactionTemplate.executeWithoutResult(status -> activityLogConsumer.consume(event, envelope));
    // Second call hits the unique constraint on event_id — the consumer must swallow the
    // DataIntegrityViolationException so the relay can move on without dead-lettering.
    transactionTemplate.executeWithoutResult(status -> activityLogConsumer.consume(event, envelope));

    assertThat(activityLogRepository.findAll()).hasSize(1);
  }
}
