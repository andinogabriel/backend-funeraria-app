package disenodesistemas.backendfunerariaapp.application.usecase.report;

import disenodesistemas.backendfunerariaapp.web.dto.response.DailyReportResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DailyReportResponseDto.PurchasesSummary;
import disenodesistemas.backendfunerariaapp.web.dto.response.DailyReportResponseDto.ServicesSummary;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Builds the daily cash reconciliation ("arqueo diario") for a single calendar day.
 *
 * <p>Follows the same read-model pattern as {@code DashboardMetricsQueryUseCase}: a thin {@code
 * @Service} that issues a handful of {@code count} / {@code sum} aggregations through {@link
 * JdbcTemplate} and assembles a response DTO. No outbound port / adapter is involved because the
 * aggregation is a cross-aggregate read (funerals + incomes) returning scalars, not entities — the
 * same shape the dashboard-metrics slice already makes against the same database. The ArchUnit
 * guardrail only forbids {@code EntityManager} in the application layer, not {@code JdbcTemplate}.
 *
 * <h3>Day bracketing</h3>
 *
 * The two date columns are stored differently, so each stream is bracketed in the matching type:
 *
 * <ul>
 *   <li><b>funerals</b> — {@code funeral_date} is a wall-clock {@code timestamp} (the local time
 *       the service was scheduled), so the day is the half-open {@link LocalDateTime} range
 *       {@code [date 00:00, date+1 00:00)} with no timezone conversion.</li>
 *   <li><b>incomes</b> — {@code income_date} stores a UTC instant. To match the operator's sense
 *       of "purchases registered on day X" we bracket the day in Argentina local time ({@link
 *       #REPORTING_ZONE}) and convert both bounds to instants, exactly as {@code IncomeQueryUseCase}
 *       does for its date-range filter, so the report and the income listing agree on what "day X"
 *       means.</li>
 * </ul>
 *
 * <h3>Reversal netting</h3>
 *
 * The active-purchases total sums {@code status = 'ACTIVE'} rows. A reversal counter-entry (created
 * by the annul flow, PR4) is itself an ACTIVE row carrying a negative {@code total_amount}, so an
 * annulled-and-reversed purchase nets to zero in the total without special-casing. The separate
 * {@code annulledCount} reports how many originals flipped to ANNULLED that day so the operator
 * sees the activity without it distorting the cash figure.
 */
@Service
@RequiredArgsConstructor
public class DailyReportQueryUseCase {

  /**
   * Timezone used to bracket the income day into absolute UTC instants. The funeral home runs out
   * of Argentina (UTC-3, no DST); the same constant anchors {@code IncomeQueryUseCase}'s date
   * filter so the daily report and the income listing agree on what "day X" means.
   */
  private static final ZoneId REPORTING_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

  private final JdbcTemplate jdbcTemplate;

  /** Builds the report for {@code date}. Read-only so the aggregations share one transaction. */
  @Transactional(readOnly = true)
  public DailyReportResponseDto buildDailyReport(final LocalDate date) {
    final ServicesSummary services = aggregateServices(date);
    final PurchasesSummary purchases = aggregatePurchases(date);
    final BigDecimal net = services.total().subtract(purchases.total());
    return new DailyReportResponseDto(date, services, purchases, net);
  }

  private ServicesSummary aggregateServices(final LocalDate date) {
    final LocalDateTime start = date.atStartOfDay();
    final LocalDateTime end = date.plusDays(1).atStartOfDay();
    final MonetaryAggregate row =
        jdbcTemplate.queryForObject(
            "select count(*), coalesce(sum(total_amount), 0) "
                + "from funeral "
                + "where deleted_at is null and funeral_date >= ? and funeral_date < ?",
            COUNT_AND_TOTAL,
            start,
            end);
    return new ServicesSummary(row.count(), row.total());
  }

  private PurchasesSummary aggregatePurchases(final LocalDate date) {
    final Timestamp start = Timestamp.from(date.atStartOfDay(REPORTING_ZONE).toInstant());
    final Timestamp end = Timestamp.from(date.plusDays(1).atStartOfDay(REPORTING_ZONE).toInstant());

    final MonetaryAggregate active =
        jdbcTemplate.queryForObject(
            "select count(*), coalesce(sum(total_amount), 0) "
                + "from incomes "
                + "where deleted = false and status = 'ACTIVE' "
                + "and income_date >= ? and income_date < ?",
            COUNT_AND_TOTAL,
            start,
            end);

    final Long annulledCount =
        jdbcTemplate.queryForObject(
            "select count(*) from incomes "
                + "where deleted = false and status = 'ANNULLED' "
                + "and income_date >= ? and income_date < ?",
            Long.class,
            start,
            end);

    return new PurchasesSummary(
        active.count(), active.total(), annulledCount == null ? 0L : annulledCount);
  }

  /** Row mapper shared by both {@code (count, sum)} aggregations. */
  private static final RowMapper<MonetaryAggregate> COUNT_AND_TOTAL =
      (rs, rowNum) -> new MonetaryAggregate(rs.getLong(1), rs.getBigDecimal(2));
}
