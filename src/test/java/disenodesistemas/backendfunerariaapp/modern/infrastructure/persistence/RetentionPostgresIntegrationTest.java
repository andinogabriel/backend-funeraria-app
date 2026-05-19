package disenodesistemas.backendfunerariaapp.modern.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.application.port.out.ActivityLogRetentionPort;
import disenodesistemas.backendfunerariaapp.application.port.out.OutboxPort;
import disenodesistemas.backendfunerariaapp.application.port.out.OutboxRetentionPort;
import disenodesistemas.backendfunerariaapp.application.usecase.metrics.ActivityFeedQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.retention.RetentionUseCase;
import disenodesistemas.backendfunerariaapp.config.RetentionProperties;
import disenodesistemas.backendfunerariaapp.domain.entity.ActivityLogEntry;
import disenodesistemas.backendfunerariaapp.domain.entity.OutboxEvent;
import disenodesistemas.backendfunerariaapp.domain.enums.OutboxStatus;
import disenodesistemas.backendfunerariaapp.domain.event.AffiliateDeleted;
import disenodesistemas.backendfunerariaapp.infrastructure.outbox.OutboxRelay;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.ActivityLogRepository;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.OutboxEventRepository;
import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * End-to-end coverage of the retention sweep against a real Postgres (ADR-0015). Drives the
 * full publish → relay → activity_log → soft delete → hard delete arc with a deterministic
 * clock so the cutoff arithmetic round-trips through the JPQL queries and the V7 partial
 * indexes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class RetentionPostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  private static final Instant NOW = Instant.parse("2026-05-19T03:30:00Z");
  private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

  @Autowired private OutboxPort outboxPort;
  @Autowired private OutboxRelay outboxRelay;
  @Autowired private OutboxEventRepository outboxRepository;
  @Autowired private ActivityLogRepository activityLogRepository;
  @Autowired private ActivityFeedQueryUseCase activityFeedQueryUseCase;
  @Autowired private OutboxRetentionPort outboxRetentionPort;
  @Autowired private ActivityLogRetentionPort activityLogRetentionPort;
  @Autowired private TransactionTemplate transactionTemplate;
  @Autowired private JdbcTemplate jdbcTemplate;

  @AfterEach
  void clearStateBetweenTests() {
    activityLogRepository.deleteAll();
    outboxRepository.deleteAll();
  }

  @Test
  @DisplayName(
      "Given an outbox row PUBLISHED 31 days ago when retention runs then it is soft-deleted (status preserved)")
  void softDeletesOldPublishedOutboxRow() {
    // Seed: publish an event and drive the relay so the row reaches PUBLISHED naturally.
    transactionTemplate.executeWithoutResult(
        status -> outboxPort.publish(new AffiliateDeleted(11111111)));
    outboxRelay.drain();
    final OutboxEvent row = outboxRepository.findAll().getFirst();
    assertThat(row.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);

    // Backdate publishedAt by 31 days so the soft cutoff (30 d) catches it.
    backdateOutboxPublishedAt(row.getId(), NOW.minus(31, ChronoUnit.DAYS));

    final int soft =
        outboxRetentionPort.softDeletePublishedBefore(
            NOW.minus(30, ChronoUnit.DAYS), NOW, 100);

    assertThat(soft).isEqualTo(1);
    final OutboxEvent refreshed = outboxRepository.findAll().getFirst();
    assertThat(refreshed.getDeletedAt()).isEqualTo(NOW);
    // Status stays PUBLISHED — soft delete is orthogonal to lifecycle.
    assertThat(refreshed.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
  }

  @Test
  @DisplayName(
      "Given an outbox row tombstoned 61 days ago when retention runs then it is hard-deleted from the table")
  void hardDeletesAgedTombstones() {
    transactionTemplate.executeWithoutResult(
        status -> outboxPort.publish(new AffiliateDeleted(22222222)));
    outboxRelay.drain();
    final OutboxEvent row = outboxRepository.findAll().getFirst();

    backdateOutboxPublishedAt(row.getId(), NOW.minus(91, ChronoUnit.DAYS));
    outboxRetentionPort.softDeletePublishedBefore(NOW.minus(30, ChronoUnit.DAYS), NOW, 100);
    // Backdate the tombstone too — the soft phase stamped it with `now`.
    backdateOutboxDeletedAt(row.getId(), NOW.minus(61, ChronoUnit.DAYS));

    final int hard = outboxRetentionPort.hardDeleteSoftDeletedBefore(
        NOW.minus(60, ChronoUnit.DAYS), 100);

    assertThat(hard).isEqualTo(1);
    assertThat(outboxRepository.findAll()).isEmpty();
  }

  @Test
  @DisplayName(
      "Given an activity_log row older than the soft cutoff when retention runs then it disappears from the feed but the row remains")
  void softDeletedActivityLogRowDoesNotAppearInFeed() {
    seedActivityLogEntry(NOW.minus(91, ChronoUnit.DAYS));

    final int soft =
        activityLogRetentionPort.softDeleteOccurredBefore(
            NOW.minus(90, ChronoUnit.DAYS), NOW, 100);

    assertThat(soft).isEqualTo(1);
    // Row still in the table (storage), but the feed query filters it out.
    assertThat(activityLogRepository.findAll()).hasSize(1);
    assertThat(activityFeedQueryUseCase.getRecentActivity(20).entries()).isEmpty();
  }

  @Test
  @DisplayName(
      "Given a PENDING outbox row when retention runs then it is NOT soft-deleted (the relay needs it)")
  void pendingRowsAreNeverSoftDeleted() {
    transactionTemplate.executeWithoutResult(
        status -> outboxPort.publish(new AffiliateDeleted(33333333)));
    final OutboxEvent row = outboxRepository.findAll().getFirst();
    assertThat(row.getStatus()).isEqualTo(OutboxStatus.PENDING);

    // Even with the cutoff set to the future (every row qualifies on date), PENDING rows
    // are excluded by the port's status filter.
    final int soft =
        outboxRetentionPort.softDeletePublishedBefore(
            NOW.plus(365, ChronoUnit.DAYS), NOW, 100);

    assertThat(soft).isZero();
    assertThat(outboxRepository.findAll().getFirst().getDeletedAt()).isNull();
  }

  @Test
  @DisplayName(
      "Given the full sweep through RetentionUseCase when it runs then every phase's count is reported")
  void fullSweepReportsCountsFromEveryPhase() {
    // 1 outbox row to soft-delete (PUBLISHED 31 d ago)
    transactionTemplate.executeWithoutResult(
        status -> outboxPort.publish(new AffiliateDeleted(44444444)));
    outboxRelay.drain();
    backdateOutboxPublishedAt(
        outboxRepository.findAll().getFirst().getId(), NOW.minus(31, ChronoUnit.DAYS));
    // 1 activity_log row to soft-delete (occurred 91 d ago)
    seedActivityLogEntry(NOW.minus(91, ChronoUnit.DAYS));

    final RetentionProperties properties =
        new RetentionProperties(
            true,
            100,
            10,
            new RetentionProperties.Window(30, 60),
            new RetentionProperties.Window(90, 90));
    final RetentionUseCase.RetentionResult result =
        new RetentionUseCase(
                outboxRetentionPort, activityLogRetentionPort, properties, FIXED_CLOCK)
            .runOnce();

    assertThat(result.outboxSoftDeleted()).isEqualTo(1);
    assertThat(result.outboxHardDeleted()).isZero(); // tombstones are fresh, not aged
    assertThat(result.activitySoftDeleted()).isEqualTo(1);
    assertThat(result.activityHardDeleted()).isZero();
  }

  // ---------- helpers ----------

  /**
   * Sneaks a backdated publishedAt onto a row via native SQL. Production code never mutates
   * publishedAt after it is stamped; the IT cheats so we can express "31 days ago" without
   * making the test sleep for 31 days. The retention queries only read these columns, so the
   * bypass is safe in this context.
   */
  private void backdateOutboxPublishedAt(final Long id, final Instant when) {
    jdbcTemplate.update(
        "update outbox_events set published_at = ? where id = ?",
        java.sql.Timestamp.from(when),
        id);
  }

  private void backdateOutboxDeletedAt(final Long id, final Instant when) {
    jdbcTemplate.update(
        "update outbox_events set deleted_at = ? where id = ?",
        java.sql.Timestamp.from(when),
        id);
  }

  private void seedActivityLogEntry(final Instant occurredAt) {
    transactionTemplate.executeWithoutResult(
        status ->
            activityLogRepository.save(
                new ActivityLogEntry(
                    UUID.randomUUID(),
                    "AFFILIATE_DELETED",
                    "AFFILIATE",
                    "99999999",
                    "Afiliado eliminado (DNI 99999999)",
                    occurredAt,
                    "trace-it")));
  }
}
