package disenodesistemas.backendfunerariaapp.application.usecase.metrics;

/**
 * Rolling time window the operator can pick per dashboard KPI card. Each value is a half-open
 * window ending at the end of "today" (exclusive upper bound), spanning {@link #days()} days
 * back. The trend percentage compares the window against the immediately-preceding window of
 * the same length.
 *
 * <p>The day counts are deliberately simple rolling windows (not calendar months / ISO weeks)
 * so the frontend can mirror the exact {@code from}/{@code to} bounds when it deep-links to the
 * filtered list view — keeping the card headline and the table it opens in agreement.
 */
public enum MetricRange {
  DAY(1),
  WEEK(7),
  MONTH(30),
  YEAR(365);

  private final int days;

  MetricRange(final int days) {
    this.days = days;
  }

  public int days() {
    return days;
  }
}
