package disenodesistemas.backendfunerariaapp.modern.security.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import disenodesistemas.backendfunerariaapp.modern.support.TestValues;
import disenodesistemas.backendfunerariaapp.security.ratelimit.LoginRateLimitProperties;
import disenodesistemas.backendfunerariaapp.security.ratelimit.LoginRateLimiter;
import disenodesistemas.backendfunerariaapp.security.ratelimit.TooManyLoginAttemptsException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LoginRateLimiter")
class LoginRateLimiterTest {

  @Test
  @DisplayName(
      "Given no previous failures when the limiter checks access then the login attempt is allowed")
  void givenNoPreviousFailuresWhenTheLimiterChecksAccessThenTheLoginAttemptIsAllowed() {
    final LoginRateLimiter rateLimiter =
        new LoginRateLimiter(
            new LoginRateLimitProperties(3, 10, 60, 15), Clock.fixed(Instant.parse("2026-04-17T10:00:00Z"), ZoneId.of("UTC")));

    assertThatNoException()
        .isThrownBy(() -> rateLimiter.assertAllowed(TestValues.USER_EMAIL, TestValues.IP_ADDRESS));
  }

  @Test
  @DisplayName(
      "Given repeated failures reaching the configured limit when access is checked then the limiter blocks the login and exposes a retry time")
  void givenRepeatedFailuresReachingTheConfiguredLimitWhenAccessIsCheckedThenTheLimiterBlocksTheLoginAndExposesARetryTime() {
    final MutableClock clock = new MutableClock(Instant.parse("2026-04-17T10:00:00Z"));
    final LoginRateLimiter rateLimiter =
        new LoginRateLimiter(new LoginRateLimitProperties(3, 10, 60, 15), clock);

    rateLimiter.onFailure(TestValues.USER_EMAIL, TestValues.IP_ADDRESS);
    rateLimiter.onFailure(TestValues.USER_EMAIL, TestValues.IP_ADDRESS);
    rateLimiter.onFailure(TestValues.USER_EMAIL, TestValues.IP_ADDRESS);

    assertThatThrownBy(() -> rateLimiter.assertAllowed(TestValues.USER_EMAIL, TestValues.IP_ADDRESS))
        .isInstanceOfSatisfying(
            TooManyLoginAttemptsException.class,
            exception -> assertThat(exception.getRetryAfterSeconds()).isGreaterThanOrEqualTo(1L));
  }

  @Test
  @DisplayName(
      "Given a locked identity when the login eventually succeeds then the limiter clears the throttling state")
  void givenALockedIdentityWhenTheLoginEventuallySucceedsThenTheLimiterClearsTheThrottlingState() {
    final MutableClock clock = new MutableClock(Instant.parse("2026-04-17T10:00:00Z"));
    final LoginRateLimiter rateLimiter =
        new LoginRateLimiter(new LoginRateLimitProperties(2, 10, 60, 15), clock);

    rateLimiter.onFailure(TestValues.USER_EMAIL, TestValues.IP_ADDRESS);
    rateLimiter.onFailure(TestValues.USER_EMAIL, TestValues.IP_ADDRESS);
    rateLimiter.onSuccess(TestValues.USER_EMAIL, TestValues.IP_ADDRESS);

    assertThatNoException()
        .isThrownBy(() -> rateLimiter.assertAllowed(TestValues.USER_EMAIL, TestValues.IP_ADDRESS));
  }

  @Test
  @DisplayName(
      "Given failures older than the sliding window when a new failure happens then the limiter resets the previous counter before evaluating the new attempt")
  void givenFailuresOlderThanTheSlidingWindowWhenANewFailureHappensThenTheLimiterResetsThePreviousCounterBeforeEvaluatingTheNewAttempt() {
    final MutableClock clock = new MutableClock(Instant.parse("2026-04-17T10:00:00Z"));
    final LoginRateLimiter rateLimiter =
        new LoginRateLimiter(new LoginRateLimitProperties(2, 10, 60, 1), clock);

    rateLimiter.onFailure(TestValues.USER_EMAIL, TestValues.IP_ADDRESS);
    clock.advanceSeconds(61);
    rateLimiter.onFailure(TestValues.USER_EMAIL, TestValues.IP_ADDRESS);

    assertThatNoException()
        .isThrownBy(() -> rateLimiter.assertAllowed(TestValues.USER_EMAIL, TestValues.IP_ADDRESS));
  }

  @Test
  @DisplayName(
      "Given a stale failure record when access is checked after the window expires then the limiter cleans it up and allows the request")
  void givenAStaleFailureRecordWhenAccessIsCheckedAfterTheWindowExpiresThenTheLimiterCleansItUpAndAllowsTheRequest() {
    final MutableClock clock = new MutableClock(Instant.parse("2026-04-17T10:00:00Z"));
    final LoginRateLimiter rateLimiter =
        new LoginRateLimiter(new LoginRateLimitProperties(3, 10, 60, 1), clock);

    rateLimiter.onFailure(TestValues.USER_EMAIL, TestValues.IP_ADDRESS);
    clock.advanceSeconds(61);

    assertThatNoException()
        .isThrownBy(() -> rateLimiter.assertAllowed(TestValues.USER_EMAIL, TestValues.IP_ADDRESS));
  }

  private static final class MutableClock extends Clock {
    private Instant instant;

    private MutableClock(final Instant instant) {
      this.instant = instant;
    }

    @Override
    public ZoneId getZone() {
      return ZoneId.of("UTC");
    }

    @Override
    public Clock withZone(final ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return instant;
    }

    private void advanceSeconds(final long seconds) {
      instant = instant.plusSeconds(seconds);
    }
  }
}
