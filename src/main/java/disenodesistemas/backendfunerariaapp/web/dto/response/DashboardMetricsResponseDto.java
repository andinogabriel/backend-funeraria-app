package disenodesistemas.backendfunerariaapp.web.dto.response;

/**
 * Aggregated KPI snapshot consumed by the operator dashboard. One field per tile rendered on
 * the bento grid; each one is a {@link KpiMetricDto} carrying value, trend and sparkline.
 *
 * <p>Read-only, cheap to compute (small counts + 8-element series), refreshed by the
 * frontend on dashboard load. There is no streaming surface yet; consumers that need
 * sub-minute freshness should poll the endpoint.
 */
public record DashboardMetricsResponseDto(
    KpiMetricDto affiliatesActive,
    KpiMetricDto plansActive,
    KpiMetricDto funeralsThisMonth,
    KpiMetricDto purchasesThisMonth,
    KpiMetricDto criticalStock,
    KpiMetricDto auditedEvents24h) {}
