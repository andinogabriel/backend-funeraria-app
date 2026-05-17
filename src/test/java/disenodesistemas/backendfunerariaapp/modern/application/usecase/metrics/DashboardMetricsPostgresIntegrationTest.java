package disenodesistemas.backendfunerariaapp.modern.application.usecase.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.application.usecase.metrics.DashboardMetricsQueryUseCase;
import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
import disenodesistemas.backendfunerariaapp.web.dto.response.DashboardMetricsResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.KpiMetricDto;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * End-to-end smoke for the dashboard metrics use case against a real PostgreSQL container.
 * The test drives the SQL directly via {@link JdbcTemplate} so the fixture stays decoupled
 * from the JPA mapper code, and exercises the four metrics under a fixed clock so the
 * "current month" / "last 24 h" windows are deterministic.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class DashboardMetricsPostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  /** Fixed clock anchor: the snapshot is built as if "now" was 2026-05-17T12:00:00Z. */
  private static final Instant NOW = Instant.parse("2026-05-17T12:00:00Z");

  private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

  @Autowired private JdbcTemplate jdbcTemplate;

  private DashboardMetricsQueryUseCase useCase;

  @BeforeEach
  void resetTables() {
    // Wipe the four tables the use case reads from. Order respects FK dependencies:
    // funerals → deceased + plans; affiliates → users + gender + relationship; audit_events
    // stands alone. We do not need to reset reference tables seeded by V2.
    jdbcTemplate.update("delete from funeral");
    jdbcTemplate.update("delete from deceased");
    jdbcTemplate.update("delete from items_plan");
    jdbcTemplate.update("delete from plans");
    jdbcTemplate.update("delete from affiliates");
    jdbcTemplate.update("delete from audit_events");
    useCase = new DashboardMetricsQueryUseCase(jdbcTemplate, FIXED_CLOCK);
  }

  @Test
  @DisplayName(
      "Given an empty database when the snapshot is built then every metric is zero with null trend and a flat zero sparkline (except plans, which ships an empty list)")
  void emptyDatabaseShipsZeroes() {
    final DashboardMetricsResponseDto snapshot = useCase.buildSnapshot();

    assertThat(snapshot.affiliatesActive().value()).isZero();
    assertThat(snapshot.affiliatesActive().trendPercent()).isNull();
    assertThat(snapshot.affiliatesActive().sparkline()).hasSize(8).allMatch(v -> v == 0L);

    assertThat(snapshot.plansActive().value()).isZero();
    assertThat(snapshot.plansActive().trendPercent()).isNull();
    assertThat(snapshot.plansActive().sparkline()).isEmpty();

    assertThat(snapshot.funeralsThisMonth().value()).isZero();
    assertThat(snapshot.funeralsThisMonth().trendPercent()).isNull();
    assertThat(snapshot.funeralsThisMonth().sparkline()).hasSize(8).allMatch(v -> v == 0L);

    assertThat(snapshot.auditedEvents24h().value()).isZero();
    assertThat(snapshot.auditedEvents24h().trendPercent()).isNull();
    assertThat(snapshot.auditedEvents24h().sparkline()).hasSize(8).allMatch(v -> v == 0L);
  }

  @Test
  @DisplayName(
      "Given seeded affiliates when the snapshot is built then active count excludes deceased rows and the 8-day sparkline reflects per-day creations")
  void affiliatesMetricReflectsSeed() {
    // Two active affiliates created today, one deceased created today (excluded), and one
    // active created six days ago — so the headline is 3 and the sparkline has 2 on the last
    // bucket, 1 on the bucket 6 days back, 0 elsewhere.
    insertAffiliate(1, "Alice", true, LocalDate.now(FIXED_CLOCK));
    insertAffiliate(2, "Bob", true, LocalDate.now(FIXED_CLOCK));
    insertAffiliate(3, "Carol", false, LocalDate.now(FIXED_CLOCK));
    insertAffiliate(4, "Dan", true, LocalDate.now(FIXED_CLOCK).minusDays(6));

    final KpiMetricDto metric = useCase.buildSnapshot().affiliatesActive();
    assertThat(metric.value()).isEqualTo(3L);
    assertThat(metric.sparkline()).hasSize(8);
    // Sparkline is oldest-first; today is the last bucket.
    assertThat(metric.sparkline().get(7)).isEqualTo(2L);
    assertThat(metric.sparkline().get(1)).isEqualTo(1L); // today - 6 days = bucket index 1
    assertThat(metric.trendPercent()).isNull();
  }

  @Test
  @DisplayName(
      "Given funerals seeded across the current and previous month when the snapshot is built then trendPercent reports the month-over-month change")
  void funeralsTrendComparesMonths() {
    final LocalDate today = LocalDate.now(FIXED_CLOCK);
    // Seed: 4 funerals this month, 2 funerals last month → trend = (4-2)/2 = 100.0%.
    insertFuneral(today.atTime(10, 0));
    insertFuneral(today.atTime(11, 0));
    insertFuneral(today.atTime(12, 0));
    insertFuneral(today.atTime(13, 0));
    final LocalDate prevMonth = today.minusMonths(1);
    insertFuneral(prevMonth.atTime(9, 0));
    insertFuneral(prevMonth.atTime(10, 0));

    final KpiMetricDto metric = useCase.buildSnapshot().funeralsThisMonth();
    assertThat(metric.value()).isEqualTo(4L);
    assertThat(metric.trendPercent()).isEqualTo(100.0);
  }

  @Test
  @DisplayName(
      "Given audit events seeded in the last 24 h and the preceding 24 h when the snapshot is built then the value counts only the recent window and the trend compares both")
  void auditEvents24hSlidingWindow() {
    // Seed: 3 events within the last 24h, 1 event in the prior 24h → trend = (3-1)/1 = 200.0%.
    insertAuditEvent(NOW.minusSeconds(3600));
    insertAuditEvent(NOW.minusSeconds(7200));
    insertAuditEvent(NOW.minusSeconds(20 * 3600));
    insertAuditEvent(NOW.minusSeconds(30 * 3600));

    final KpiMetricDto metric = useCase.buildSnapshot().auditedEvents24h();
    assertThat(metric.value()).isEqualTo(3L);
    assertThat(metric.trendPercent()).isEqualTo(200.0);
    assertThat(metric.sparkline()).hasSize(8);
    // The 24 h window split in 8 buckets = 3-hour stride. The two events at -1h and -2h fall
    // into the most recent bucket; the event at -20h falls into one of the earlier buckets.
    assertThat(metric.sparkline().get(7)).isEqualTo(2L);
    assertThat(metric.sparkline().stream().mapToLong(Long::longValue).sum()).isEqualTo(3L);
  }

  /* ----------------------------------- helpers ---------------------------------- */

  /** Inserts an affiliate skipping the optional FKs we don't need for the metric. */
  private void insertAffiliate(
      final long id, final String firstName, final boolean active, final LocalDate startDate) {
    jdbcTemplate.update(
        "insert into affiliates (id, first_name, last_name, dni, birth_date, start_date, deceased)"
            + " values (?, ?, 'Smith', ?, ?, ?, ?)",
        id,
        firstName,
        (int) (10_000_000 + id),
        Date.valueOf(LocalDate.of(1980, 1, 1)),
        Date.valueOf(startDate),
        !active);
  }

  /** Inserts a funeral with only the columns relevant to the metric. */
  private void insertFuneral(final LocalDateTime registerDate) {
    jdbcTemplate.update(
        "insert into funeral (funeral_date, register_date, receipt_number, receipt_series,"
            + " tax, total_amount)"
            + " values (?, ?, ?, '0001', 21, 0)",
        registerDate,
        registerDate,
        "REC-" + java.util.UUID.randomUUID());
  }

  /** Inserts a minimal audit event with the required columns from V3. */
  private void insertAuditEvent(final Instant occurredAt) {
    jdbcTemplate.update(
        "insert into audit_events (id, occurred_at, actor_email, action, target_type, target_id)"
            + " values (nextval('audit_events_seq'), ?, 'fixture@example.com', 'FUNERAL_CREATED',"
            + " 'FUNERAL', '1')",
        Timestamp.from(occurredAt));
  }
}
