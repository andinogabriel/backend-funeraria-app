package disenodesistemas.backendfunerariaapp.modern.application.usecase.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.application.usecase.metrics.MonthlyTrendsQueryUseCase;
import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
import disenodesistemas.backendfunerariaapp.web.dto.response.MonthlyTrendPointDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MonthlyTrendsDto;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Runs the monthly trends SQL against a real PostgreSQL container under a fixed clock
 * (2026-11-09), seeding rows with plain JDBC so the fixture is independent of the JPA mappings.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MonthlyTrendsPostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  private static final Instant NOW = Instant.parse("2026-11-09T12:00:00Z");
  private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

  @Autowired private JdbcTemplate jdbcTemplate;

  private MonthlyTrendsQueryUseCase useCase;

  @BeforeEach
  void resetTables() {
    jdbcTemplate.update("delete from membership_payments");
    jdbcTemplate.update("delete from membership_dues");
    jdbcTemplate.update("delete from funeral");
    jdbcTemplate.update("delete from incomes");
    jdbcTemplate.update("delete from affiliates where dni = 41999001");
    jdbcTemplate.update("delete from membership_holders where dni = 21999001");
    useCase = new MonthlyTrendsQueryUseCase(jdbcTemplate, FIXED_CLOCK);
  }

  @Test
  @DisplayName(
      "Given an empty database when the trends are built then every month up to the current one is present with zeroes")
  void emptyDatabaseShipsContinuousZeroes() {
    final MonthlyTrendsDto trends = useCase.monthlyTrends(12);

    assertThat(trends.asOf()).isEqualTo(LocalDate.of(2026, 11, 9));
    assertThat(trends.points()).hasSize(12);
    assertThat(trends.points().getFirst().month()).isEqualTo("2025-12");
    assertThat(trends.points().getLast().month()).isEqualTo("2026-11");
    assertThat(trends.points())
        .allSatisfy(
            p -> {
              assertThat(p.funerals()).isZero();
              assertThat(p.funeralRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
              assertThat(p.purchases()).isEqualByComparingTo(BigDecimal.ZERO);
              assertThat(p.duesBilled()).isEqualByComparingTo(BigDecimal.ZERO);
              assertThat(p.duesCollected()).isEqualByComparingTo(BigDecimal.ZERO);
            });
  }

  @Test
  @DisplayName("Given an out-of-range month count when the trends are built then it is clamped")
  void clampsTheMonthCount() {
    assertThat(useCase.monthlyTrends(0).points()).hasSize(MonthlyTrendsQueryUseCase.MIN_MONTHS);
    assertThat(useCase.monthlyTrends(500).points()).hasSize(MonthlyTrendsQueryUseCase.MAX_MONTHS);
  }

  @Test
  @DisplayName(
      "Given services, purchases, dues and payments across months when the trends are built then each lands in its month and excluded rows do not count")
  void aggregatesEachSeriesByMonth() {
    insertFuneral(LocalDateTime.of(2026, 11, 2, 10, 0), "150000.00", false);
    insertFuneral(LocalDateTime.of(2026, 11, 5, 10, 0), "50000.50", false);
    insertFuneral(LocalDateTime.of(2026, 11, 6, 10, 0), "999999.00", true);
    insertFuneral(LocalDateTime.of(2026, 9, 30, 23, 0), "80000.00", false);
    // Outside a 3-month window: must not appear anywhere.
    insertFuneral(LocalDateTime.of(2026, 8, 31, 23, 0), "70000.00", false);

    insertIncome(LocalDateTime.of(2026, 10, 3, 9, 0), "ACTIVE", false, "30000.00", null);
    insertIncome(LocalDateTime.of(2026, 10, 4, 9, 0), "ACTIVE", false, "-5000.00", null);
    insertIncome(LocalDateTime.of(2026, 10, 22, 9, 0), "ACTIVE", true, "54321.00", null);
    // An annulled purchase and its reversal: neither side counts, whatever the months.
    final long annulled =
        insertIncome(LocalDateTime.of(2026, 10, 21, 9, 0), "ANNULLED", false, "12345.00", null);
    insertIncome(LocalDateTime.of(2026, 11, 2, 9, 0), "ACTIVE", false, "-12345.00", annulled);

    final long[] ids = insertHolderAndAffiliate();
    insertDue(ids, LocalDate.of(2026, 10, 1), "2600.00");
    insertDue(ids, LocalDate.of(2026, 11, 1), "2600.00");
    // Paid in November, settling October: counts as November cash.
    insertPayment(ids[0], LocalDate.of(2026, 11, 3), "2600.00");
    insertPayment(ids[0], LocalDate.of(2026, 11, 8), "1000.00");

    final List<MonthlyTrendPointDto> points = useCase.monthlyTrends(3).points();

    assertThat(points).extracting(MonthlyTrendPointDto::month)
        .containsExactly("2026-09", "2026-10", "2026-11");

    final MonthlyTrendPointDto september = points.get(0);
    assertThat(september.funerals()).isEqualTo(1L);
    assertThat(september.funeralRevenue()).isEqualByComparingTo("80000.00");

    final MonthlyTrendPointDto october = points.get(1);
    assertThat(october.funerals()).isZero();
    assertThat(october.purchases()).isEqualByComparingTo("25000.00");
    assertThat(october.duesBilled()).isEqualByComparingTo("2600.00");
    assertThat(october.duesCollected()).isEqualByComparingTo(BigDecimal.ZERO);

    final MonthlyTrendPointDto november = points.get(2);
    assertThat(november.funerals()).isEqualTo(2L);
    assertThat(november.funeralRevenue()).isEqualByComparingTo("200000.50");
    assertThat(november.purchases()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(november.duesBilled()).isEqualByComparingTo("2600.00");
    assertThat(november.duesCollected()).isEqualByComparingTo("3600.00");
  }

  private void insertFuneral(
      final LocalDateTime registerDate, final String total, final boolean binned) {
    jdbcTemplate.update(
        "insert into funeral (funeral_date, register_date, receipt_number, receipt_series,"
            + " tax, total_amount, deleted_at)"
            + " values (?, ?, ?, '0001', 21, ?, ?)",
        registerDate,
        registerDate,
        "REC-" + UUID.randomUUID(),
        new BigDecimal(total),
        binned ? java.sql.Timestamp.from(NOW) : null);
  }

  private long insertIncome(
      final LocalDateTime incomeDate,
      final String status,
      final boolean deleted,
      final String total,
      final Long reversalOf) {
    return jdbcTemplate.queryForObject(
        "insert into incomes (id, deleted, tax, total_amount, income_date, receipt_number,"
            + " receipt_series, status, reversal_of_id)"
            + " values (nextval('incomes_seq'), ?, 21, ?, ?, ?, 1, ?, ?) returning id",
        Long.class,
        deleted,
        new BigDecimal(total),
        incomeDate,
        (long) (System.nanoTime() & 0x7fffffff),
        status,
        reversalOf);
  }

  /** Returns {holderId, affiliateId, healthTierId}. */
  private long[] insertHolderAndAffiliate() {
    final Long holderId =
        jdbcTemplate.queryForObject(
            "insert into membership_holders (dni, first_name, last_name)"
                + " values (21999001, 'Rosa', 'Trend') returning id",
            Long.class);
    final Long tierId =
        jdbcTemplate.queryForObject(
            "select id from health_tiers where code = 'STANDARD'", Long.class);
    final Long affiliateId =
        jdbcTemplate.queryForObject(
            "insert into affiliates (id, first_name, last_name, dni, birth_date, start_date,"
                + " deceased, holder_id, health_tier_id)"
                + " values (9990001, 'Ana', 'Trend', 41999001, ?, ?, false, ?, ?) returning id",
            Long.class,
            Date.valueOf(LocalDate.of(1980, 1, 1)),
            Date.valueOf(LocalDate.of(2025, 1, 1)),
            holderId,
            tierId);
    return new long[] {holderId, affiliateId, tierId};
  }

  private void insertDue(final long[] ids, final LocalDate period, final String amount) {
    jdbcTemplate.update(
        "insert into membership_dues (affiliate_id, holder_id, period, due_date, amount,"
            + " health_tier_id) values (?, ?, ?, ?, ?, ?)",
        ids[1],
        ids[0],
        period,
        period.plusDays(9),
        new BigDecimal(amount),
        ids[2]);
  }

  private void insertPayment(final long holderId, final LocalDate paidOn, final String amount) {
    jdbcTemplate.update(
        "insert into membership_payments (holder_id, amount, unallocated, paid_on, method,"
            + " registered_by) values (?, ?, 0, ?, 'CASH', 'it@example.com')",
        holderId,
        new BigDecimal(amount),
        paidOn);
  }
}
