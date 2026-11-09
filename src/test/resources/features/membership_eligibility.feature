Feature: Claim eligibility lookup is restricted to admins

  As the funeral home desk
  I want the membership eligibility lookup to be admin-only and to fail clearly on unknown DNIs
  So that coverage verdicts are neither leaked to regular users nor silently invented

  Scenario: A regular user cannot query claim eligibility
    Given a non-admin user "clerk@bdd.local" is authenticated
    When the claim eligibility of DNI 42000001 is requested
    Then the eligibility request is denied

  Scenario: An unknown DNI is reported as not found
    Given an admin "admin@bdd.local" is authenticated
    When the claim eligibility of DNI 99999998 is requested
    Then the eligibility request fails with "affiliate.error.not.found"
