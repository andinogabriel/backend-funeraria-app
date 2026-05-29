package disenodesistemas.backendfunerariaapp.domain.enums;

/**
 * Catalog of notification kinds the in-app alert center can carry. The wire shape stays
 * a free-form discriminator (varchar on the DB column) so a future PR can drop in a new
 * type without a migration; the enum is the closed list of values the writer side knows
 * how to construct.
 */
public enum NotificationType {
  /**
   * An item's stock crossed its configured {@code low_stock_threshold} downward. The
   * payload carries the item id / code / name / current stock / threshold so the
   * frontend can render the alert without a follow-up lookup.
   */
  LOW_STOCK_REACHED
}
