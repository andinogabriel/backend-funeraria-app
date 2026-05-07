Feature: Audit log records administrative role grants

  As a compliance reviewer
  I want every role grant performed by an admin to leave an audit trail
  So that I can reconstruct who granted which privilege and when

  Background:
    Given an admin "admin@bdd.local" is authenticated
    And a regular user "auditee@bdd.local" exists in the system

  Scenario: Granting a role emits an audit event
    When the admin grants the "ROLE_ADMIN" role to "auditee@bdd.local"
    Then an audit event with action "USER_ROLE_GRANTED" is recorded for "auditee@bdd.local"
    And the audit event payload references the granted role "ROLE_ADMIN"

  Scenario: Granting a role that is already assigned does not duplicate the audit event
    When the admin grants the "ROLE_USER" role to "auditee@bdd.local"
    And the admin grants the "ROLE_USER" role to "auditee@bdd.local"
    Then exactly 1 audit event with action "USER_ROLE_GRANTED" exists for "auditee@bdd.local"
