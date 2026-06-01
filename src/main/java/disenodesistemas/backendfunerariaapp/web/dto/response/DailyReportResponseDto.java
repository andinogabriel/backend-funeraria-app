package disenodesistemas.backendfunerariaapp.web.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Daily cash reconciliation ("arqueo diario") returned by {@code GET /api/v1/reports/daily}.
 *
 * <p>The funeral home's cash flow has two opposing streams on any given calendar day:
 *
 * <ul>
 *   <li><b>services</b> — money IN: funeral services dated on the day, summed over
 *       {@code funeral.total_amount}. Soft-deleted funerals are excluded.</li>
 *   <li><b>purchases</b> — money OUT: stock bought from suppliers, summed over
 *       {@code incomes.total_amount} for {@code status = ACTIVE} rows. Reversal counter-entries
 *       (PR4) are ACTIVE rows carrying a negative amount, so they net out of the active total
 *       automatically when a purchase is annulled; {@code annulledCount} surfaces how many
 *       originals were cancelled that day for the operator's awareness without double-counting.</li>
 * </ul>
 *
 * <p>{@code net} is {@code services.total - purchases.total} — the day's net cash movement.
 * Positive means the home took in more than it spent on stock.
 */
public record DailyReportResponseDto(
    LocalDate date, ServicesSummary services, PurchasesSummary purchases, BigDecimal net) {

  /**
   * Funeral-service revenue for the day.
   *
   * @param count number of non-deleted funerals dated on the day
   * @param total sum of their {@code total_amount} (never null; zero when empty)
   */
  public record ServicesSummary(long count, BigDecimal total) {}

  /**
   * Supplier-purchase outflow for the day.
   *
   * @param count number of ACTIVE income rows dated on the day (includes reversal counter-entries)
   * @param total sum of their {@code total_amount}; reversal rows contribute negative amounts so an
   *     annulled-and-reversed purchase nets to zero (never null; zero when empty)
   * @param annulledCount number of ANNULLED originals dated on the day, surfaced for awareness
   */
  public record PurchasesSummary(long count, BigDecimal total, long annulledCount) {}
}
