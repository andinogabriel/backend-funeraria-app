package disenodesistemas.backendfunerariaapp.application.usecase.metrics;

import disenodesistemas.backendfunerariaapp.web.dto.response.MonthlyTrendPointDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MonthlyTrendsDto;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Month-by-month business trends for the dashboard charts: services rendered and their
 * revenue, supplier purchases, and membership dues billed against dues collected.
 *
 * <p>Every series covers the same calendar months, oldest first, ending with the current month
 * (which is partial). A month without rows is reported as zero rather than skipped, so the
 * chart's x axis is continuous.
 *
 * <ul>
 *   <li><b>Services</b>: funerals by {@code register_date}, excluding the ones in the bin.
 *   <li><b>Purchases</b>: incomes that still stand, by {@code income_date}: active, not
 *       deleted and not a reversal counter-entry. Annulling a purchase marks the original
 *       {@code ANNULLED} and adds an active, negative reversal; summing that reversal would
 *       subtract a purchase that is already excluded, so both sides of an annulment drop out.
 *   <li><b>Dues billed</b>: membership dues by the period they bill.
 *   <li><b>Dues collected</b>: membership payments by the day they were paid. This is cash
 *       received in the month, which may settle older periods, so it is not the paid share of
 *       that month's dues.
 * </ul>
 *
 * <p>Like {@link DashboardMetricsQueryUseCase}, it reads aggregates straight through
 * {@link JdbcTemplate}: four grouped queries by date column, no entity graph. The {@code date}
 * columns are cast to {@code timestamp} before {@code date_trunc}, which would otherwise go
 * through {@code timestamptz} and depend on the session time zone.
 */
@Service
@RequiredArgsConstructor
public class MonthlyTrendsQueryUseCase {

  public static final int DEFAULT_MONTHS = 12;
  public static final int MIN_MONTHS = 3;
  public static final int MAX_MONTHS = 24;

  private final JdbcTemplate jdbcTemplate;
  private final Clock clock;

  /**
   * Builds the trends for the last {@code months} calendar months including the current one.
   * Out-of-range values are clamped to [{@link #MIN_MONTHS}, {@link #MAX_MONTHS}].
   */
  @Transactional(readOnly = true)
  public MonthlyTrendsDto monthlyTrends(final int months) {
    final int span = Math.clamp(months, MIN_MONTHS, MAX_MONTHS);
    final YearMonth last = YearMonth.now(clock);
    final YearMonth first = last.minusMonths(span - 1L);
    final Date from = Date.valueOf(first.atDay(1));
    final Date to = Date.valueOf(last.plusMonths(1).atDay(1));

    final Map<YearMonth, Long> funerals = new HashMap<>();
    final Map<YearMonth, BigDecimal> funeralRevenue = new HashMap<>();
    jdbcTemplate.query(
        "select date_trunc('month', register_date)::date as month, count(*) as total,"
            + " coalesce(sum(total_amount), 0) as revenue"
            + " from funeral where deleted_at is null"
            + " and register_date >= ? and register_date < ? group by 1",
        rs -> {
          final YearMonth month = month(rs);
          funerals.put(month, rs.getLong("total"));
          funeralRevenue.put(month, rs.getBigDecimal("revenue"));
        },
        from,
        to);

    final Map<YearMonth, BigDecimal> purchases =
        sumByMonth(
            "select date_trunc('month', income_date)::date as month,"
                + " coalesce(sum(total_amount), 0) as amount"
                + " from incomes where deleted = false and status = 'ACTIVE'"
                + " and reversal_of_id is null"
                + " and income_date >= ? and income_date < ? group by 1",
            from,
            to);
    final Map<YearMonth, BigDecimal> duesBilled =
        sumByMonth(
            "select date_trunc('month', period::timestamp)::date as month, coalesce(sum(amount), 0) as amount"
                + " from membership_dues where period >= ? and period < ? group by 1",
            from,
            to);
    final Map<YearMonth, BigDecimal> duesCollected =
        sumByMonth(
            "select date_trunc('month', paid_on::timestamp)::date as month,"
                + " coalesce(sum(amount), 0) as amount"
                + " from membership_payments where paid_on >= ? and paid_on < ? group by 1",
            from,
            to);

    final List<MonthlyTrendPointDto> points = new ArrayList<>(span);
    for (YearMonth month = first; !month.isAfter(last); month = month.plusMonths(1)) {
      points.add(
          new MonthlyTrendPointDto(
              month.toString(),
              funerals.getOrDefault(month, 0L),
              funeralRevenue.getOrDefault(month, BigDecimal.ZERO),
              purchases.getOrDefault(month, BigDecimal.ZERO),
              duesBilled.getOrDefault(month, BigDecimal.ZERO),
              duesCollected.getOrDefault(month, BigDecimal.ZERO)));
    }
    return new MonthlyTrendsDto(LocalDate.now(clock), List.copyOf(points));
  }

  private Map<YearMonth, BigDecimal> sumByMonth(final String sql, final Date from, final Date to) {
    final Map<YearMonth, BigDecimal> totals = new HashMap<>();
    jdbcTemplate.query(
        sql,
        (RowCallbackHandler) rs -> totals.put(month(rs), rs.getBigDecimal("amount")),
        from,
        to);
    return totals;
  }

  private static YearMonth month(final ResultSet rs) throws SQLException {
    return YearMonth.from(rs.getDate("month").toLocalDate());
  }
}
