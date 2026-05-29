package disenodesistemas.backendfunerariaapp.domain.event;

/**
 * Emitted when an item's stock <em>crosses</em> the configured low-stock threshold
 * downward. The trigger fires only on the cross, not on every below-threshold change
 * — see the design decision baked into {@code LowStockDetectionService}: an item that
 * was already at 3 with threshold 10 and goes to 2 does <em>not</em> re-emit.
 *
 * <p>Carries enough payload for the {@code NotificationConsumer} to materialise a
 * notification row without an extra item lookup ({@code id}, {@code code}, {@code name},
 * threshold + before/after stock for the "fell from X to Y" rendering).
 */
public record LowStockReached(
    Long itemId,
    String code,
    String name,
    Integer threshold,
    Integer stockBefore,
    Integer stockAfter)
    implements DomainEvent {

  @Override
  public String aggregateType() {
    return "ITEM";
  }

  @Override
  public String aggregateId() {
    return String.valueOf(itemId);
  }

  @Override
  public String eventType() {
    return "LOW_STOCK_REACHED";
  }
}
