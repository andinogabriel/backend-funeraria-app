Feature: Signup
  Scenario: Signup user correctly.
    Given an email not registered and a password "validPassLength123"
    When the user tries to signup
    Then the user registers successfully