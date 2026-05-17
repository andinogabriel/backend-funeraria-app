package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Wire shape for one KPI tile on the operator dashboard. Carries the headline value, the
 * optional period-over-period trend and a small sparkline series — same layout used by the
 * `<app-kpi-tile>` Angular component on the consumer side.
 *
 * <p>The {@code trendPercent} field is nullable because not every metric has a comparable
 * previous window in the current schema (e.g. {@code Plan} has no audit timestamps so a
 * "vs. previous period" delta cannot be computed). The {@code sparkline} list is always
 * present but may be empty for the same reason; consumers render the value alone in that
 * case.
 *
 * @param value current snapshot value of the metric (e.g. "afiliados activos right now")
 * @param trendPercent percentage change vs. the comparable previous window; {@code null}
 *     when a previous window does not exist or its value is zero (cannot divide)
 * @param sparkline ordered series for the inline sparkline, oldest entry first; bucket size
 *     is metric-specific (daily for most, hourly for the 24-hour audit-event metric)
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record KpiMetricDto(long value, Double trendPercent, List<Long> sparkline) {}
