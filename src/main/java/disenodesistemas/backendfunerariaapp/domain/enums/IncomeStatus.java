package disenodesistemas.backendfunerariaapp.domain.enums;

/**
 * Lifecycle state of an {@code incomes} row.
 *
 * <ul>
 *   <li>{@link #ACTIVE} — the income (compra) is live and contributes to the catalog
 *       stock + analytics. Reversal rows are also {@code ACTIVE}: they are real
 *       accounting entries, not a tombstone.</li>
 *   <li>{@link #ANNULLED} — the income was cancelled by an operator. The row stays
 *       visible (badge "Anulado") so audit reads can trace the cancellation, but it
 *       no longer counts as an active receipt. A matching reversal row with the same
 *       {@code reversal_of_id} pointer carries the negative-quantity counter-entry
 *       that compensates the original stock + price effects.</li>
 * </ul>
 */
public enum IncomeStatus {
  ACTIVE,
  ANNULLED
}
