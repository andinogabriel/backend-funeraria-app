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
  FUNERAL_STATE_CHANGED
}
