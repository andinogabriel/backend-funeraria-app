package disenodesistemas.backendfunerariaapp.web.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
 *
 * <p>Each summary also carries the per-row {@code lines} that make up its total, so the operator
 * can drill into a card on the arqueo page without a second round-trip. A day rarely has more than
 * a handful of services / purchases, so shipping the lines inline keeps the contract simple at a
 * negligible payload cost.
 */
public record DailyReportResponseDto(
    LocalDate date, ServicesSummary services, PurchasesSummary purchases, BigDecimal net) {

  /**
   * Funeral-service revenue for the day.
   *
   * @param count number of non-deleted funerals dated on the day
   * @param total sum of their {@code total_amount} (never null; zero when empty)
   * @param lines the individual funerals that make up the total, newest first
   */
  public record ServicesSummary(long count, BigDecimal total, List<ServiceLine> lines) {}

  /**
   * Supplier-purchase outflow for the day.
   *
   * @param count number of ACTIVE income rows dated on the day (includes reversal counter-entries)
   * @param total sum of their {@code total_amount}; reversal rows contribute negative amounts so an
   *     annulled-and-reversed purchase nets to zero (never null; zero when empty)
   * @param annulledCount number of ANNULLED originals dated on the day, surfaced for awareness
   * @param lines the individual incomes that make up the total — ACTIVE originals, reversal
   *     counter-entries (negative) and ANNULLED originals — newest first, so the operator sees the
   *     full picture and can reconcile the netting by eye
   */
  public record PurchasesSummary(
      long count, BigDecimal total, long annulledCount, List<PurchaseLine> lines) {}

  /**
   * One funeral service in the day's detail.
   *
   * @param funeralId the funeral's database id, so the frontend can deep-link to
   *     {@code /servicios/:id} (the funeral detail route is keyed by id, not receipt number)
   * @param receiptNumber the funeral's receipt number (e.g. {@code F-99121})
   * @param deceasedName full name of the deceased the service was for
   * @param planName name of the plan sold, or {@code null} if the funeral has no linked plan
   * @param amount the funeral's {@code total_amount}
   */
  public record ServiceLine(
      long funeralId,
      String receiptNumber,
      String deceasedName,
      String planName,
      BigDecimal amount) {}

  /**
   * One supplier purchase in the day's detail.
   *
   * @param receiptNumber the income's receipt number
   * @param supplierName name of the supplier, or {@code null} if the income has no linked supplier
   * @param amount the income's {@code total_amount}; negative for reversal counter-entries
   * @param status lifecycle state — {@code ACTIVE} or {@code ANNULLED}
   * @param reversal {@code true} when this row is a reversal counter-entry (carries a negative
   *     amount and a back-pointer to the annulled original)
   */
  public record PurchaseLine(
      String receiptNumber,
      String supplierName,
      BigDecimal amount,
      String status,
      boolean reversal) {}
}
