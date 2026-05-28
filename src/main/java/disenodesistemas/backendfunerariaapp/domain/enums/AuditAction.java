package disenodesistemas.backendfunerariaapp.domain.enums;

/**
 * Closed catalog of business operations recorded in the audit log. Each value identifies a
 * specific sensitive admin event the application captures for compliance and forensic analysis.
 * The set is deliberately enumerated rather than free-form so reports and dashboards can group
 * by a stable code, and so future additions are reviewed alongside the use case that emits them.
 */
public enum AuditAction {
  /** A role was granted to a user. The payload typically lists the role name added. */
  USER_ROLE_GRANTED,

  /** A role was revoked from a user. The payload typically lists the role name removed. */
  USER_ROLE_REVOKED,

  /** A user account moved from pending to active after email confirmation. */
  USER_ACTIVATED,

  /** A new affiliate record was created. */
  AFFILIATE_CREATED,

  /** An existing affiliate record was deleted. */
  AFFILIATE_DELETED,

  /** A new funeral record was created. */
  FUNERAL_CREATED,

  /** An existing funeral record was deleted. */
  FUNERAL_DELETED,

  /** A funeral transitioned to a new business state. The payload carries the previous and new state. */
  FUNERAL_STATE_CHANGED,

  /** A new plan record was created. */
  PLAN_CREATED,

  /** An existing plan record was deleted. */
  PLAN_DELETED,

  /** A new item record was created. */
  ITEM_CREATED,

  /** An existing item record was deleted. */
  ITEM_DELETED,

  /**
   * An existing income (compra) was annulled by an admin. The payload carries the
   * {@code originalId} and {@code reversalId} so audit consumers can join the pair
   * without an extra lookup.
   */
  INCOME_ANNULLED,

  /**
   * The per-item {@code low_stock_threshold} was updated by an admin (either through
   * the Item edit form or inline from an Income line). Payload carries the previous
   * and new values so audit consumers can see how the threshold evolved without
   * joining back to the items table.
   */
  ITEM_THRESHOLD_UPDATED
}
