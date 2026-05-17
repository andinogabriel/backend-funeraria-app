package disenodesistemas.backendfunerariaapp.application.usecase.metrics;

import disenodesistemas.backendfunerariaapp.web.dto.response.DashboardMetricsResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.KpiMetricDto;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aggregates the four KPIs the operator dashboard surfaces (afiliados activos, planes
 * activos, servicios del mes, eventos auditados en 24 h) into a single
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
public class DashboardMetricsQueryUseCase {

  private static final int DAILY_BUCKETS = 8;
  private static final int HOURLY_BUCKETS = 8;

  private final JdbcTemplate jdbcTemplate;
  private final Clock clock;

  /**
   * Production-time constructor wired by Spring; defaults the clock to {@link Clock#systemUTC()}
   * so the use case behaves correctly without an extra {@code @Bean} declaration. Tests use the
   * package-private overload below to inject a fixed clock.
   */
  @Autowired
  public DashboardMetricsQueryUseCase(final JdbcTemplate jdbcTemplate) {
    this(jdbcTemplate, Clock.systemUTC());
  }

  /** Test-friendly overload that lets a fixed clock drive the "current" window boundaries. */
  public DashboardMetricsQueryUseCase(final JdbcTemplate jdbcTemplate, final Clock clock) {
    this.jdbcTemplate = jdbcTemplate;
    this.clock = clock;
  }

  /** Builds the dashboard snapshot. Runs read-only so the four count queries share a tx. */
  @Transactional(readOnly = true)
  public DashboardMetricsResponseDto buildSnapshot() {
    return new DashboardMetricsResponseDto(
        affiliatesActiveMetric(),
        plansActiveMetric(),
        funeralsThisMonthMetric(),
        auditedEvents24hMetric());
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
