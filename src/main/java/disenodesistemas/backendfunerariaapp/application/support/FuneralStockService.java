package disenodesistemas.backendfunerariaapp.application.support;

import disenodesistemas.backendfunerariaapp.domain.entity.Plan;

/**
 * Inventory-side counterpart for the funeral (venta) flow. Mirrors the
 * {@link IncomeDetailService} contract but in the opposite direction: a funeral
 * <b>consumes</b> stock when it is sold, and <b>returns</b> stock to the catalog when
 * it is cancelled (soft-deleted) or its plan is swapped on update.
 *
 * <h3>Why a plan-driven snapshot</h3>
 *
 * A funeral references a {@link Plan} aggregate by FK. The plan in turn carries the
 * canonical list of items + quantities the operator agreed to deliver. We do <em>not</em>
 * snapshot the per-item composition into a per-funeral table (yet) because every
 * other read in the system already trusts the plan as the source of truth at any moment
 * — historical funerals already show the plan's current items, not the items as they
 * were at sale time.
 *
 * <p>The trade-off: if a plan is edited between when a funeral was sold and when it is
 * cancelled, the stock restored on cancel will reflect the <em>current</em> plan
 * composition, not the one that was actually decremented at sale. Stock can drift over
 * the long run. Mitigations:
 *
 * <ul>
 *   <li>Plans are rarely edited in practice — the operator usually creates a new plan
 *       instead of mutating an existing one.</li>
 *   <li>The per-funeral snapshot can be added later (new table + Flyway migration)
 *       without breaking this contract: the service interface stays the same, only the
 *       impl swaps the plan lookup for a snapshot lookup.</li>
 *   <li>Stock is allowed to go negative on purpose (decision: a funeral cannot be
 *       refused because the system shows insufficient stock — reality dictates).</li>
 * </ul>
 *
 * <h3>What this service does NOT do</h3>
 *
 * <ul>
 *   <li>Update prices. The income flow refreshes {@code item.price} from each receipt;
 *       a funeral sells at whatever the price was that day and never touches it.</li>
 *   <li>Validate stock. Negative stock is acceptable; the future low-stock notification
 *       (PR5b) is what alerts admins when stock crosses a threshold downward.</li>
 * </ul>
 */
public interface FuneralStockService {

  /**
   * Decrements each item's stock by the {@code quantity} that the plan bundles. Called
   * after a funeral is persisted (on create), or after the funeral's plan is swapped to
   * a different one on update.
   *
   * <p>Items with no stock value yet (null) are treated as 0 — the decrement still
   * goes through, leaving the stock at {@code -quantity}, which the low-stock alert
   * surfaces.
   */
  void applyStockForFuneral(Plan plan);

  /**
   * Increments each item's stock by the {@code quantity} that the plan bundles. Called
   * before a funeral is soft-deleted (so the catalog reflects the cancellation), or
   * before the funeral's plan is swapped to a different one on update (rollback of the
   * previous decrement).
   */
  void restoreStockForFuneral(Plan plan);
}
