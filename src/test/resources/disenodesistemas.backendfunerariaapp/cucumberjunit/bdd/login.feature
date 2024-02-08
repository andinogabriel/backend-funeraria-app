Feature: Login
  Scenario: Admin login
    Given admin user wants to login
    When the user tries to login as admin
    Then the user has admin role.