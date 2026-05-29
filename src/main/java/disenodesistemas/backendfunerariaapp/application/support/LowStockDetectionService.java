package disenodesistemas.backendfunerariaapp.application.support;

import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;

/**
 * Decides whether a stock change on a given item crossed the configured low-stock
 * threshold downward and, if so, publishes a {@code LowStockReached} domain event.
 *
 * <p>The check is consciously narrow: it fires <em>only</em> on the crossing transition,
 * not on every below-threshold change. An item that was already at 3 with threshold 10
 * and goes to 2 does not re-emit; an item that goes from 11 to 8 does. The rule keeps
 * the audit / notification stream signal-rich — a single alert per cross is exactly
 * what the operator needs to investigate; repeated below-threshold writes would just
 * spam the bell icon.
 *
 * <p>Implementation lives in {@link disenodesistemas.backendfunerariaapp.application.support.impl.LowStockDetectionServiceImpl};
 * the interface stays at the application boundary so the inbound stock-mutating
 * services (funeral / income) depend on the contract, not the concrete consumer of the
 * outbox event.
 */
public interface LowStockDetectionService {

  /**
   * Compares the item's stock at {@code stockBefore} and {@code stockAfter} against
   * {@code item.lowStockThreshold} and publishes a {@code LowStockReached} event when
   * the value crossed the threshold downward. Null stocks are coerced to 0 — a never-
   * stocked item that gets its first negative write counts as a cross.
   */
  void detectAndPublish(ItemEntity item, int stockBefore, int stockAfter);
}
