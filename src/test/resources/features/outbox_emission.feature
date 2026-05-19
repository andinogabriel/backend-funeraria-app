Feature: Domain events are published to the outbox when affiliate mutations succeed

  As a downstream integrator
  I want every affiliate mutation to record a typed domain event in the outbox
  So that I can rebuild a customer-side projection without re-reading the affiliate table

  Background:
    Given an admin "admin@bdd.local" is authenticated

  Scenario: Creating an affiliate publishes an AffiliateCreated event to the outbox
    When the admin creates an affiliate with dni 40111222 named "Roberto" "Fernández"
    Then exactly 1 outbox event of type "AFFILIATE_CREATED" is pending for aggregate "AFFILIATE" id "40111222"
    And the AffiliateCreated outbox payload for aggregate id "40111222" carries the affiliate's first name "Roberto"

  Scenario: Renaming an affiliate publishes an AffiliateUpdated event
    Given an affiliate with dni 40555666 named "Marcos" "Díaz" already exists
    When the admin renames the affiliate with dni 40555666 to "Marcos Daniel" "Díaz"
    Then exactly 1 outbox event of type "AFFILIATE_UPDATED" is pending for aggregate "AFFILIATE" id "40555666"
    And no outbox event of type "AFFILIATE_MARKED_DECEASED" is pending for aggregate "AFFILIATE" id "40555666"

  Scenario: Deleting an affiliate publishes an AffiliateDeleted event
    Given an affiliate with dni 40777888 named "Sofía" "Castro" already exists
    When the admin deletes the affiliate with dni 40777888
    Then exactly 1 outbox event of type "AFFILIATE_DELETED" is pending for aggregate "AFFILIATE" id "40777888"

  Scenario: A create + a delete leave exactly one PENDING event per kind for the affected dni
    When the admin creates an affiliate with dni 40999000 named "Lucas" "Martínez"
    And the admin deletes the affiliate with dni 40999000
    Then exactly 1 outbox event of type "AFFILIATE_CREATED" is pending for aggregate "AFFILIATE" id "40999000"
    And exactly 1 outbox event of type "AFFILIATE_DELETED" is pending for aggregate "AFFILIATE" id "40999000"
