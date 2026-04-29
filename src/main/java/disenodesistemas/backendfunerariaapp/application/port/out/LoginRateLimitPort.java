package disenodesistemas.backendfunerariaapp.application.port.out;

/**
 * Outbound port for login-attempt throttling. Application orchestration depends on this contract
 * so the rate-limiting strategy (exponential backoff, distributed counters, external service, ...)
 * can evolve inside the infrastructure layer without leaking concrete adapter types into use
 * cases.
 *
 * <p>Implementations are expected to combine the email and IP address into a single tracked key
 * and to throw when the caller is currently locked.
 */
public interface LoginRateLimitPort {

  /**
   * Verifies that another login attempt is currently allowed for the supplied principal/IP pair.
   * Implementations throw a domain-aware exception when the caller must wait before retrying.
   */
  void assertAllowed(String email, String ip);

  /**
   * Records a failed authentication attempt so the limiter can extend the active backoff window
   * for future calls to {@link #assertAllowed(String, String)}.
   */
  void onFailure(String email, String ip);

  /**
   * Resets the tracked limiter state after a successful authentication so a legitimate user is
   * not penalized by previously accumulated failures.
   */
  void onSuccess(String email, String ip);
}
