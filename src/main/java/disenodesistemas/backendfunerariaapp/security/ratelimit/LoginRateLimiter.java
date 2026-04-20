package disenodesistemas.backendfunerariaapp.security.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Applies exponential throttling to login attempts grouped by email and IP address. The limiter is
 * designed to slow down repeated authentication failures before they hit deeper business logic,
 * while still allowing the counters to recover automatically after the configured time window.
 */
@Component
public class LoginRateLimiter {

  private record Key(String email, String ip) {}

  private static final class State {
    private int failures;
    private Instant firstFailureAt;
    private Instant lockedUntil;

    /**
     * Returns whether the tracked failure window has already expired for the supplied instant. Once
     * the window is outside range, the accumulated failures can be safely discarded and the caller
     * may start from a clean rate-limiting state.
     */
    synchronized boolean isOutsideWindow(final Instant now, final Duration window) {
      return firstFailureAt != null && firstFailureAt.plus(window).isBefore(now);
    }

    /**
     * Returns how many seconds remain until the active lock expires for this state entry. The
     * result is normalized to at least one second when the lock is active so callers can expose a
     * meaningful retry hint to clients.
     */
    synchronized long retryAfterSeconds(final Instant now) {
      if (lockedUntil == null || !lockedUntil.isAfter(now)) {
        return 0L;
      }
      return Math.max(Duration.between(now, lockedUntil).toSeconds(), 1L);
    }

    /**
     * Registers a new failed attempt and recalculates the exponential lock duration if required.
     * The lock grows after the configured threshold so repeated failures become progressively more
     * expensive while still being capped by a configurable maximum duration.
     */
    synchronized void registerFailure(
        final Instant now,
        final Duration window,
        final int maxFailures,
        final int baseLockSeconds,
        final int maxLockSeconds) {
      if (firstFailureAt == null || firstFailureAt.plus(window).isBefore(now)) {
        firstFailureAt = now;
        failures = 0;
        lockedUntil = null;
      }

      failures++;

      if (failures >= maxFailures) {
        final int exponent = Math.max(0, failures - maxFailures);
        long lockSeconds = (long) baseLockSeconds * (1L << Math.min(exponent, 10));
        lockSeconds = Math.min(lockSeconds, maxLockSeconds);
        lockedUntil = now.plusSeconds(lockSeconds);
      }
    }
  }

  private final LoginRateLimitProperties props;
  private final Clock clock;
  private final ConcurrentMap<Key, State> states = new ConcurrentHashMap<>();

  /**
   * Creates the rate limiter using the system UTC clock for time calculations. Production code
   * uses this constructor while tests can inject a custom clock through the overload below for
   * deterministic assertions around rate-limiting windows.
   */
  public LoginRateLimiter(final LoginRateLimitProperties props) {
    this(props, Clock.systemUTC());
  }

  /**
   * Creates the rate limiter with an explicit clock implementation. Providing the clock externally
   * makes the limiter easier to test and keeps all duration calculations based on a single,
   * injectable time source.
   */
  public LoginRateLimiter(final LoginRateLimitProperties props, final Clock clock) {
    this.props = Objects.requireNonNull(props, "props");
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  /**
   * Ensures the next login attempt is still allowed for the supplied email and IP pair. If the
   * caller is currently locked, the method throws an exception containing the remaining backoff
   * duration so the API can communicate when another attempt may be retried.
   */
  public void assertAllowed(final String email, final String ip) {
    final Instant now = clock.instant();
    final Key key = new Key(normalize(email), normalize(ip));
    final State state = states.get(key);
    if (state == null) {
      return;
    }

    final Duration window = Duration.ofMinutes(props.windowMinutes());
    if (state.isOutsideWindow(now, window)) {
      states.remove(key, state);
      return;
    }

    final long retryAfterSeconds = state.retryAfterSeconds(now);
    if (retryAfterSeconds > 0) {
      throw new TooManyLoginAttemptsException("auth.login.rate.limit.exceeded", retryAfterSeconds);
    }
  }

  /**
   * Clears the tracked limiter state after a successful authentication. This resets the failure
   * history for the email and IP combination so legitimate users are not kept under an old
   * backoff once they prove ownership of correct credentials.
   */
  public void onSuccess(final String email, final String ip) {
    states.remove(new Key(normalize(email), normalize(ip)));
  }

  /**
   * Records a failed authentication attempt for the supplied email and IP pair. The method updates
   * the current state window and, when needed, extends the exponential lock that future attempts
   * will observe through {@link #assertAllowed(String, String)}.
   */
  public void onFailure(final String email, final String ip) {
    final Instant now = clock.instant();
    final Key key = new Key(normalize(email), normalize(ip));
    final State state = states.computeIfAbsent(key, ignored -> new State());
    final Duration window = Duration.ofMinutes(props.windowMinutes());
    state.registerFailure(
        now,
        window,
        props.maxFailures(),
        props.baseLockSeconds(),
        props.maxLockSeconds());
  }

  /**
   * Normalizes input values before they are used as part of the limiter key. This avoids
   * fragmenting counters due to blanks or inconsistent casing and keeps rate-limiting behavior
   * stable across semantically identical requests.
   */
  private static String normalize(final String value) {
    if (StringUtils.isBlank(value)) {
      return "-";
    }
    return value.trim().toLowerCase();
  }
}
