package disenodesistemas.backendfunerariaapp.application.usecase.metrics;

/**
 * The dashboard KPIs that support an operator-selectable {@link MetricRange}. Each maps onto a
 * timestamped fact table the range query counts over:
 *
 * <ul>
 *   <li>{@link #SERVICES} — funerals registered ({@code funeral.register_date}).</li>
 *   <li>{@link #PURCHASES} — ACTIVE, non-deleted supplier incomes ({@code incomes.income_date}).</li>
 *   <li>{@link #AUDIT} — audit events ({@code audit_events.occurred_at}).</li>
 * </ul>
 *
 * The stock-figure KPIs (afiliados, planes, stock critico) have no meaningful time window and
 * are intentionally absent.
 */
public enum MetricKind {
  SERVICES,
  PURCHASES,
  AUDIT
}
