package disenodesistemas.backendfunerariaapp.application.usecase.user;

import disenodesistemas.backendfunerariaapp.application.model.RequestMetadata;
import disenodesistemas.backendfunerariaapp.application.port.out.LoginRateLimitPort;
import disenodesistemas.backendfunerariaapp.application.port.out.SecurityThreatProtectionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Centralizes the security guards that sit around session-related use cases before domain logic is
 * executed. It combines login throttling with adaptive threat protection so suspicious activity can
 * be tracked, escalated and blocked consistently across login and authenticated requests.
 */
@Service
@RequiredArgsConstructor
public class UserSessionSecurityService {

  private final SecurityThreatProtectionPort securityThreatProtectionPort;
  private final LoginRateLimitPort loginRateLimiter;

  /**
   * Applies all security checks that must succeed before a login attempt can continue. It combines
   * blacklist enforcement with rate limiting so the authentication path can stop abusive traffic
   * before the password validation step touches the user aggregate.
   */
  public void assertLoginAllowed(
      final String email, final String deviceId, final RequestMetadata requestMetadata) {
    securityThreatProtectionPort.assertLoginAllowed(email, deviceId, requestMetadata);
    loginRateLimiter.assertAllowed(email, requestMetadata.ipAddress());
  }

  /**
   * Registers a failed login in both security subsystems involved in threat detection. The method
   * updates the exponential limiter and the adaptive blacklist tracker so repeated failures can
   * progressively escalate from throttling into temporary blocking.
   */
  public void recordLoginFailure(
      final String email, final String deviceId, final RequestMetadata requestMetadata) {
    loginRateLimiter.onFailure(email, requestMetadata.ipAddress());
    securityThreatProtectionPort.recordLoginFailure(email, deviceId, requestMetadata);
  }

  /**
   * Clears any accumulated failure state after a successful authentication. This resets the rate
   * limiter and the adaptive tracker for the caller so legitimate users are not penalized after
   * proving ownership of valid credentials.
   */
  public void recordLoginSuccess(
      final String email, final String deviceId, final RequestMetadata requestMetadata) {
    loginRateLimiter.onSuccess(email, requestMetadata.ipAddress());
    securityThreatProtectionPort.recordLoginSuccess(email, deviceId, requestMetadata);
  }

  /**
   * Applies post-authentication threat checks to requests that already carry a valid identity. The
   * method is used by JWT validation flows to ensure that blacklisted principals, devices or IPs
   * cannot continue using an otherwise well-formed token.
   */
  public void assertRequestAllowed(
      final String principal, final String deviceId, final RequestMetadata requestMetadata) {
    securityThreatProtectionPort.assertRequestAllowed(principal, deviceId, requestMetadata);
  }

  /**
   * Reports a suspicious signal to the adaptive threat layer for future blocking decisions. This
   * is used when the request exhibits anomalies such as device mismatch or invalid fingerprint
   * state and the system needs to accumulate evidence before or while blacklisting.
   */
  public void recordSuspiciousRequest(
      final String principal,
      final String deviceId,
      final RequestMetadata requestMetadata,
      final String reason) {
    securityThreatProtectionPort.recordSuspiciousRequest(
        principal, deviceId, requestMetadata, reason);
  }
}
