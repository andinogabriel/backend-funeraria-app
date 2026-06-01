package disenodesistemas.backendfunerariaapp.modern.application.usecase.report;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.application.usecase.report.DailyReportQueryUseCase;
import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
import disenodesistemas.backendfunerariaapp.web.dto.response.DailyReportResponseDto;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Real-Postgres coverage for the daily-report aggregations. Seeds funeral + income rows via {@link
 * JdbcTemplate} and asserts the use case's SQL: soft-delete exclusion, the Argentina-zone day
 * bracket for incomes, reversal netting on the active-purchases total, and the empty-day zero path.
 *
 * <p>Mirrors {@code DashboardMetricsPostgresIntegrationTest}: {@code webEnvironment = NONE}, the
 * tables are wiped in {@code @BeforeEach} (FK-safe order), and the use case is constructed directly
 * over the autowired {@link JdbcTemplate}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class DailyReportQueryUseCasePostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  private static final ZoneId REPORTING_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

  @Autowired private JdbcTemplate jdbcTemplate;

  private DailyReportQueryUseCase useCase;

  @BeforeEach
  void resetTables() {
    jdbcTemplate.update("delete from income_details");
    jdbcTemplate.update("delete from incomes");
    jdbcTemplate.update("delete from funeral");
    jdbcTemplate.update("delete from deceased");
    useCase = new DailyReportQueryUseCase(jdbcTemplate);
  }

  @Test
  @DisplayName(
      "Given funerals on the day plus a soft-deleted and a next-day row, when aggregating services, then only the two active same-day rows are summed")
  void services_sum_excludes_soft_deleted_and_other_days() {
    final LocalDate day = LocalDate.of(2026, 6, 1);
    insertFuneral("F-A", day.atStartOfDay().plusHours(10), "500000.00", null);
    insertFuneral("F-B", day.atStartOfDay().plusHours(15), "300000.00", null);
    // Soft-deleted funeral on the same day — excluded.
    insertFuneral(
        "F-DEL",
        day.atStartOfDay().plusHours(11),
        "999.00",
        Timestamp.from(day.atStartOfDay(REPORTING_ZONE).toInstant()));
    // Next-day funeral — outside the half-open range.
    insertFuneral("F-NEXT", day.plusDays(1).atStartOfDay().plusHours(9), "777.00", null);

    final DailyReportResponseDto report = useCase.buildDailyReport(day);

    assertThat(report.services().count()).isEqualTo(2);
    assertThat(report.services().total()).isEqualByComparingTo("800000.00");
  }

  @Test
  @DisplayName(
      "Given active purchases, a negative reversal, an annulled original and a 22:00 ART row, when aggregating purchases, then the total nets the reversal, includes the late-evening row and counts annulled separately")
  void purchases_active_total_nets_reversals_and_counts_annulled_separately() {
    final LocalDate day = LocalDate.of(2026, 6, 2);
    insertIncome(910001, day, 9, "200000.00", "ACTIVE");
    insertIncome(910002, day, 12, "150000.00", "ACTIVE");
    // Annulled original — counted in annulledCount, NOT in the active total.
    insertIncome(910003, day, 13, "80000.00", "ANNULLED");
    // Its reversal counter-entry: ACTIVE, negative amount — nets out of the active total.
    insertIncome(910004, day, 14, "-80000.00", "ACTIVE");
    // Late-evening income at 22:00 ART = 01:00Z of the NEXT UTC day. A naive UTC bracket would
    // wrongly exclude it, so this row proves the day is bracketed in Argentina local time.
    insertIncome(910006, day, 22, "10000.00", "ACTIVE");
    // Next-day income — outside the bracket on either interpretation.
    insertIncome(910005, day.plusDays(1), 9, "999.00", "ACTIVE");

    final DailyReportResponseDto report = useCase.buildDailyReport(day);

    // Active total: 200000 + 150000 + (-80000) + 10000 = 280000
    assertThat(report.purchases().total()).isEqualByComparingTo("280000.00");
    // Four ACTIVE rows that day (three purchases + one reversal).
    assertThat(report.purchases().count()).isEqualTo(4);
    assertThat(report.purchases().annulledCount()).isEqualTo(1);
  }

  @Test
  @DisplayName(
      "Given an active day and a separate empty day, when building the report, then net is services minus purchases and the empty day is all zeroes")
  void net_is_services_minus_purchases_and_empty_day_is_zero() {
    final LocalDate active = LocalDate.of(2026, 6, 3);
    insertFuneral("F-NET", active.atStartOfDay().plusHours(10), "1000000.00", null);
    insertIncome(910100, active, 11, "350000.00", "ACTIVE");

    final DailyReportResponseDto report = useCase.buildDailyReport(active);
    assertThat(report.net()).isEqualByComparingTo("650000.00");

    final DailyReportResponseDto empty = useCase.buildDailyReport(LocalDate.of(2026, 6, 4));
    assertThat(empty.services().count()).isZero();
    assertThat(empty.services().total()).isEqualByComparingTo("0");
    assertThat(empty.purchases().count()).isZero();
    assertThat(empty.purchases().total()).isEqualByComparingTo("0");
    assertThat(empty.purchases().annulledCount()).isZero();
    assertThat(empty.net()).isEqualByComparingTo("0");
  }

  private void insertFuneral(
      final String receipt,
      final LocalDateTime funeralDate,
      final String totalAmount,
      final Timestamp deletedAt) {
    jdbcTemplate.update(
        "insert into funeral (funeral_date, register_date, receipt_number, receipt_series,"
            + " tax, total_amount, deleted_at) values (?, ?, ?, 'T', 21, ?, ?)",
        Timestamp.valueOf(funeralDate),
        Timestamp.valueOf(funeralDate),
        receipt,
        new BigDecimal(totalAmount),
        deletedAt);
  }

  /**
   * Inserts an income at {@code hourArt} on {@code day} in Argentina local time, converted to the
   * UTC instant the column stores, so the row lands inside the use case's Argentina-zone bracket.
   */
  private void insertIncome(
      final long receiptNumber,
      final LocalDate day,
      final int hourArt,
      final String totalAmount,
      final String status) {
    final Timestamp incomeDate =
        Timestamp.from(day.atStartOfDay(REPORTING_ZONE).plusHours(hourArt).toInstant());
    jdbcTemplate.update(
        "insert into incomes (id, deleted, tax, total_amount, income_date, receipt_number,"
            + " receipt_series, status) values (nextval('incomes_seq'), false, 21, ?, ?, ?, 1001, ?)",
        new BigDecimal(totalAmount),
        incomeDate,
        receiptNumber,
        status);
  }
}
