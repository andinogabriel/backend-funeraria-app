Feature: Monthly dashboard trends are restricted to admins

  As the funeral home owner
  I want the monthly revenue, purchases and dues trends to be admin-only
  So that financial figures are not shown to regular users

  Scenario: A regular user cannot read the monthly trends
    Given a non-admin user "clerk@bdd.local" is authenticated
    When the monthly trends for 6 months are requested
    Then the trends request is denied

  Scenario: An admin reads one point per month, current month last
    Given an admin "admin@bdd.local" is authenticated
    When the monthly trends for 6 months are requested
    Then the trends cover 6 consecutive months ending with the current one
