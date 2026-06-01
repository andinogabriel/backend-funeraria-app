package disenodesistemas.backendfunerariaapp.application.usecase.metrics;

import disenodesistemas.backendfunerariaapp.web.dto.response.DashboardMetricsResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.KpiMetricDto;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aggregates the KPIs the operator dashboard surfaces (afiliados activos, planes activos,
 * servicios del mes, compras del mes, stock critico, eventos auditados en 24 h) into a single
 * {@link DashboardMetricsResponseDto}. The query is intentionally one-shot — consumers call
 * it on dashboard load and again on the refresh action; no caching layer is involved because
 * the underlying counts are cheap (small tables, hot indexes) and a real-time figure beats a
 * lagged-by-minutes Caffeine entry for the operator UX.
 *
 * <h3>Sparkline buckets</h3>
 *
 * Three of the four metrics return an 8-day daily bucket series — one bucket per day, oldest
 * first, so the renderer can plot a 1-week-plus-1-day window. The audit-event metric returns
 * 8 hourly buckets because the surface is "last 24 h" and an 8-day daily series would dwarf
 * the headline figure. Buckets are computed with PostgreSQL's {@code date_trunc('day', ...)};
 * the application currently runs against Postgres in every environment (dev, IT, prod) so
 * portability concerns do not apply.
 *
 * <h3>Trend</h3>
 *
 * Two of the four metrics ship a trend percentage. Funerals compare current calendar month
 * vs. previous calendar month; audit events compare the last 24 h vs. the preceding 24 h.
 * Affiliates and plans return {@code null} for trend in v1: the affiliate snapshot is a
 * stock figure (active right now) that does not have a meaningful "previous window", and
 * plans have no audit timestamp to anchor a comparison.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardMetricsQueryUseCase {

  private static final int DAILY_BUCKETS = 8;
  private static final int HOURLY_BUCKETS = 8;

  private final JdbcTemplate jdbcTemplate;
  /**
   * Wall-clock read used to anchor the "current" window boundaries (current
   * month for funerals, last 24 h for audit events). Wired from the shared
   * {@code TimeConfig} bean ({@link Clock#systemUTC()} in production, fixed at
   * a known instant in tests).
   */
  private final Clock clock;

  /** Builds the dashboard snapshot. Runs read-only so the count queries share a tx. */
  @Transactional(readOnly = true)
  public DashboardMetricsResponseDto buildSnapshot() {
    return new DashboardMetricsResponseDto(
        affiliatesActiveMetric(),
        plansActiveMetric(),
        funeralsThisMonthMetric(),
        purchasesThisMonthMetric(),
        criticalStockMetric(),
        auditedEvents24hMetric());
  }

  /* -------------------------------- range-selectable metric ------------------------------ */

  /**
   * Recomputes a single time-windowed KPI ({@link MetricKind}) over an operator-selected
   * rolling {@link MetricRange}. Backs the per-card range dropdown on the dashboard: when the
   * operator switches "Servicios del mes" to a yearly window, the frontend calls this with
   * {@code (SERVICES, YEAR)} and the card re-renders without reloading the whole snapshot.
   *
   * <p>The window is half-open {@code [start, end)} where {@code end} is the start of tomorrow
   * (so "today" is fully included) and {@code start} is {@code range.days()} days earlier. The
   * trend compares it against the immediately-preceding window of the same length; the sparkline
   * splits the window into {@link #DAILY_BUCKETS} equal buckets, oldest first.
   */
  @Transactional(readOnly = true)
  public KpiMetricDto rangeMetric(final MetricKind kind, final MetricRange range) {
    final LocalDate today = LocalDate.now(clock);
    final LocalDateTime end = today.plusDays(1).atStartOfDay();
    final LocalDateTime start = end.minusDays(range.days());
    final LocalDateTime previousStart = start.minusDays(range.days());

    final long current = countInWindow(kind, start, end);
    final long previous = countInWindow(kind, previousStart, start);
    final Double trend = trendPercent(current, previous);

    final List<Long> sparkline = new ArrayList<>(DAILY_BUCKETS);
    final long totalSeconds = java.time.Duration.between(start, end).getSeconds();
    final long step = Math.max(1, totalSeconds / DAILY_BUCKETS);
    for (int i = 0; i < DAILY_BUCKETS; i++) {
      final LocalDateTime bucketStart = start.plusSeconds(i * step);
      final LocalDateTime bucketEnd = i == DAILY_BUCKETS - 1 ? end : bucketStart.plusSeconds(step);
      sparkline.add(countInWindow(kind, bucketStart, bucketEnd));
    }
    return new KpiMetricDto(current, trend, List.copyOf(sparkline));
  }

  private long countInWindow(
      final MetricKind kind, final LocalDateTime start, final LocalDateTime end) {
    return switch (kind) {
      case SERVICES -> countFuneralsBetween(start, end);
      case PURCHASES -> countPurchasesBetween(start, end);
      case AUDIT ->
          countAuditBetween(
              start.atZone(clock.getZone()).toInstant(), end.atZone(clock.getZone()).toInstant());
    };
  }

  /* -------------------------------- affiliates ------------------------------------------- */

  /**
   * Active affiliates: not deceased, with the 8-day daily creation sparkline so an operator
   * sees the alta cadence at a glance. Trend stays {@code null} because the headline is a
   * stock figure (active right now), not a flow.
   */
  private KpiMetricDto affiliatesActiveMetric() {
    final long active =
        firstLongOrZero(
            jdbcTemplate.queryForList(
                "select count(*) from affiliates where coalesce(deceased, false) = false",
                Long.class));
    final List<Long> sparkline =
        dailyBuckets(
            "select count(*) from affiliates "
                + "where start_date >= ? and start_date < ? "
                + "and coalesce(deceased, false) = false");
    return new KpiMetricDto(active, null, sparkline);
  }

  /* -------------------------------- plans ------------------------------------------------ */

  /**
   * Plans: total active count. Plans carry no audit timestamps yet, so neither sparkline nor
   * trend are computable; the response ships an empty list and {@code null} trend.
   */
  private KpiMetricDto plansActiveMetric() {
    final long total =
        firstLongOrZero(jdbcTemplate.queryForList("select count(*) from plans", Long.class));
    return new KpiMetricDto(total, null, List.of());
  }

  /* -------------------------------- funerals --------------------------------------------- */

  /**
   * Funerals registered in the current calendar month, with the 8-day daily registration
   * sparkline and a month-over-month trend percentage.
   */
  private KpiMetricDto funeralsThisMonthMetric() {
    final LocalDate today = LocalDate.now(clock);
    final LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
    final LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);
    final LocalDateTime startOfPrevMonth = startOfMonth.minusMonths(1);

    final long current = countFuneralsBetween(startOfMonth, startOfNextMonth);
    final long previous = countFuneralsBetween(startOfPrevMonth, startOfMonth);
    final Double trend = trendPercent(current, previous);
    final List<Long> sparkline =
        dailyBuckets(
            "select count(*) from funeral where register_date >= ? and register_date < ?");
    return new KpiMetricDto(current, trend, sparkline);
  }

  private long countFuneralsBetween(final LocalDateTime start, final LocalDateTime end) {
    return firstLongOrZero(
        jdbcTemplate.queryForList(
            "select count(*) from funeral where register_date >= ? and register_date < ?",
            Long.class,
            start,
            end));
  }

  /* -------------------------------- purchases (incomes) ---------------------------------- */

  /**
   * Supplier purchases (ACTIVE incomes) registered in the current calendar month — money out, the
   * mirror of {@link #funeralsThisMonthMetric()} (money in). Counts ACTIVE rows only so annulled
   * originals do not inflate the figure; the reversal counter-entries are ACTIVE but they cancel an
   * earlier purchase, so the count is a deliberate "how many purchase events happened" rather than a
   * net-of-reversals number (the arqueo report owns the monetary netting). Ships the 8-day daily
   * sparkline + a month-over-month trend, same shape as funerals.
   *
   * <p>{@code income_date} is a UTC instant column; the dashboard already brackets every other
   * window with {@link LocalDateTime} bounds built from the application clock, so we keep the same
   * convention here for consistency across the bento (a few hours of UTC-vs-local skew at the month
   * boundary is immaterial to a monthly headcount).
   */
  private KpiMetricDto purchasesThisMonthMetric() {
    final LocalDate today = LocalDate.now(clock);
    final LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
    final LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);
    final LocalDateTime startOfPrevMonth = startOfMonth.minusMonths(1);

    final long current = countPurchasesBetween(startOfMonth, startOfNextMonth);
    final long previous = countPurchasesBetween(startOfPrevMonth, startOfMonth);
    final Double trend = trendPercent(current, previous);
    final List<Long> sparkline =
        dailyBuckets(
            "select count(*) from incomes "
                + "where deleted = false and status = 'ACTIVE' "
                + "and income_date >= ? and income_date < ?");
    return new KpiMetricDto(current, trend, sparkline);
  }

  private long countPurchasesBetween(final LocalDateTime start, final LocalDateTime end) {
    return firstLongOrZero(
        jdbcTemplate.queryForList(
            "select count(*) from incomes "
                + "where deleted = false and status = 'ACTIVE' "
                + "and income_date >= ? and income_date < ?",
            Long.class,
            start,
            end));
  }

  /* -------------------------------- critical stock --------------------------------------- */

  /**
   * Items at or below their configured low-stock threshold (PR5a) — the operator's "what do I need
   * to restock" headline. Excludes soft-deleted items (papelera) and items with a null stock
   * (catalog entries without inventory, e.g. services, which cannot be "low"). A stock figure with
   * no meaningful previous window, so trend + sparkline stay empty like {@link #plansActiveMetric()}.
   */
  private KpiMetricDto criticalStockMetric() {
    final long critical =
        firstLongOrZero(
            jdbcTemplate.queryForList(
                "select count(*) from items "
                    + "where deleted_at is null and stock is not null "
                    + "and stock <= low_stock_threshold",
                Long.class));
    return new KpiMetricDto(critical, null, List.of());
  }

  /* -------------------------------- audit events ----------------------------------------- */

  /**
   * Audit events captured in the last 24 hours, with 8 hourly buckets covering those 24 h
   * (3-hour stride) and a trend vs. the preceding 24 h.
   */
  private KpiMetricDto auditedEvents24hMetric() {
    final Instant now = Instant.now(clock);
    final Instant windowStart = now.minusSeconds(24L * 3600);
    final Instant prevWindowStart = windowStart.minusSeconds(24L * 3600);

    final long current = countAuditBetween(windowStart, now);
    final long previous = countAuditBetween(prevWindowStart, windowStart);
    final Double trend = trendPercent(current, previous);

    final List<Long> sparkline = new ArrayList<>(HOURLY_BUCKETS);
    // 24 h split into 8 buckets = one bucket every 3 hours.
    final long bucketSeconds = (24L * 3600) / HOURLY_BUCKETS;
    for (int i = 0; i < HOURLY_BUCKETS; i++) {
      final Instant bucketStart = windowStart.plusSeconds(i * bucketSeconds);
      final Instant bucketEnd = bucketStart.plusSeconds(bucketSeconds);
      sparkline.add(countAuditBetween(bucketStart, bucketEnd));
    }
    return new KpiMetricDto(current, trend, List.copyOf(sparkline));
  }

  private long countAuditBetween(final Instant from, final Instant to) {
    return firstLongOrZero(
        jdbcTemplate.queryForList(
            "select count(*) from audit_events where occurred_at >= ? and occurred_at < ?",
            Long.class,
            java.sql.Timestamp.from(from),
            java.sql.Timestamp.from(to)));
  }

  /* -------------------------------- shared helpers --------------------------------------- */

  /**
   * Runs the supplied {@code count(*)} query parameterised on a half-open day range, eight
   * times in a row, and returns the resulting series oldest-first. Uses {@link LocalDateTime}
   * boundaries built from the application clock so a fixed-clock test can produce a
   * deterministic series.
   */
  private List<Long> dailyBuckets(final String countSql) {
    final LocalDate today = LocalDate.now(clock);
    final List<Long> series = new ArrayList<>(DAILY_BUCKETS);
    for (int offset = DAILY_BUCKETS - 1; offset >= 0; offset--) {
      final LocalDateTime dayStart = today.minusDays(offset).atStartOfDay();
      final LocalDateTime dayEnd = dayStart.plusDays(1);
      final long count =
          firstLongOrZero(jdbcTemplate.queryForList(countSql, Long.class, dayStart, dayEnd));
      series.add(count);
    }
    return List.copyOf(series);
  }

  /** Returns {@code null} when {@code previous} is zero (cannot divide); else the % change. */
  private static Double trendPercent(final long current, final long previous) {
    if (previous == 0) {
      return null;
    }
    final double delta = ((double) current - previous) / previous;
    return Math.round(delta * 1000.0) / 10.0;
  }

  private static long firstLongOrZero(final List<Long> rows) {
    if (rows.isEmpty() || rows.get(0) == null) {
      return 0L;
    }
    return rows.get(0);
  }
}
