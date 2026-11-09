package disenodesistemas.backendfunerariaapp.modern.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.web.controller.MembershipBillingController;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Step definitions for {@code GET /api/v1/membership/eligibility} (ADR-0022). The BDD context runs
 * without a web server, so the steps call the controller bean: the class-level
 * {@code @PreAuthorize("hasRole('ADMIN')")} still applies through the method-security proxy, which
 * is exactly the boundary under test. The security context itself is reset by the
 * {@code @Before} hook in {@link AuditLogStepDefinitions}.
 */
public class MembershipEligibilityStepDefinitions {

  private final MembershipBillingController controller;

  /** Outcome of the last request: null when it answered, otherwise what it threw. */
  private RuntimeException failure;

  @Autowired
  public MembershipEligibilityStepDefinitions(final MembershipBillingController controller) {
    this.controller = controller;
  }

  @Given("a non-admin user {string} is authenticated")
  public void aNonAdminUserIsAuthenticated(final String email) {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(email, null, "ROLE_USER"));
  }

  @When("the claim eligibility of DNI {int} is requested")
  public void theClaimEligibilityIsRequested(final int dni) {
    failure = null;
    try {
      controller.eligibility(dni, null, null);
    } catch (final RuntimeException e) {
      failure = e;
    }
  }

  @Then("the eligibility request is denied")
  public void theRequestIsDenied() {
    assertThat(failure).isInstanceOf(AccessDeniedException.class);
  }

  @Then("the eligibility request fails with {string}")
  public void theRequestFailsWith(final String messageKey) {
    assertThat(failure).isInstanceOf(NotFoundException.class).hasMessage(messageKey);
  }
}
