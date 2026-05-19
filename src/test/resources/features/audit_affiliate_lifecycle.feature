Feature: Audit log records affiliate lifecycle operations

  As a compliance reviewer
  I want every affiliate creation and deletion performed by an admin to leave an audit trail
  So that I can reconstruct who registered or removed each affiliate and when

  Background:
    Given an admin "admin@bdd.local" is authenticated

  Scenario: Creating a new affiliate records an AFFILIATE_CREATED audit event
    When the admin creates an affiliate with dni 30111222 named "Juan" "Pérez"
    Then an audit event with action "AFFILIATE_CREATED" is recorded for affiliate dni 30111222
    And the actor on the AFFILIATE_CREATED audit event for dni 30111222 is "admin@bdd.local"

  Scenario: Deleting an existing affiliate records an AFFILIATE_DELETED audit event
    Given an affiliate with dni 30333444 named "María" "Gomez" already exists
    When the admin deletes the affiliate with dni 30333444
    Then an audit event with action "AFFILIATE_DELETED" is recorded for affiliate dni 30333444
    And the actor on the AFFILIATE_DELETED audit event for dni 30333444 is "admin@bdd.local"

  Scenario: Two creations followed by a deletion produce two CREATED entries and one DELETED entry
    When the admin creates an affiliate with dni 30555666 named "Carlos" "López"
    And the admin creates an affiliate with dni 30777888 named "Ana" "Suárez"
    And the admin deletes the affiliate with dni 30555666
    Then exactly 1 audit event with action "AFFILIATE_CREATED" exists for affiliate dni 30555666
    And exactly 1 audit event with action "AFFILIATE_CREATED" exists for affiliate dni 30777888
    And exactly 1 audit event with action "AFFILIATE_DELETED" exists for affiliate dni 30555666
