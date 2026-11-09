package disenodesistemas.backendfunerariaapp.modern.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.web.controller.MetricsController;
import disenodesistemas.backendfunerariaapp.web.dto.response.MonthlyTrendPointDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MonthlyTrendsDto;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.Clock;
import java.time.YearMonth;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

/**
 * Step definitions for {@code GET /api/v1/metrics/trends}. Like the eligibility steps, they call
 * the controller bean so its {@code @PreAuthorize("hasRole('ADMIN')")} applies through the
 * method-security proxy. The authentication steps and the security-context reset live in
 * {@link MembershipEligibilityStepDefinitions} and {@link AuditLogStepDefinitions}.
 */
public class DashboardTrendsStepDefinitions {

  private final MetricsController controller;
  private final Clock clock;

  private MonthlyTrendsDto trends;
  private RuntimeException failure;

  @Autowired
  public DashboardTrendsStepDefinitions(final MetricsController controller, final Clock clock) {
    this.controller = controller;
    this.clock = clock;
  }

  @When("the monthly trends for {int} months are requested")
  public void theMonthlyTrendsAreRequested(final int months) {
    trends = null;
    failure = null;
    try {
      trends = controller.trends(months).getBody();
    } catch (final RuntimeException e) {
      failure = e;
    }
  }

  @Then("the trends request is denied")
  public void theTrendsRequestIsDenied() {
    assertThat(failure).isInstanceOf(AccessDeniedException.class);
  }

  @Then("the trends cover {int} consecutive months ending with the current one")
  public void theTrendsCoverConsecutiveMonths(final int months) {
    assertThat(failure).isNull();
    final YearMonth current = YearMonth.now(clock);
    assertThat(trends.points())
        .extracting(MonthlyTrendPointDto::month)
        .containsExactly(
            IntStream.range(0, months)
                .mapToObj(i -> current.minusMonths(months - 1L - i).toString())
                .toArray(String[]::new));
  }
}
