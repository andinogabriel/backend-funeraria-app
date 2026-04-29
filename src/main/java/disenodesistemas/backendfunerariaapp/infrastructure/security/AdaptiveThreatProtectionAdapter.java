package disenodesistemas.backendfunerariaapp.infrastructure.security;

import disenodesistemas.backendfunerariaapp.application.model.RequestMetadata;
import disenodesistemas.backendfunerariaapp.application.port.out.SecurityThreatProtectionPort;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.ThreatProtectionProperties;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Implements adaptive threat protection by aggregating failed logins and suspicious request
 * signals across principals, devices and IP addresses. Once configured thresholds are reached, it
 * creates short-lived blacklist entries that can proactively block further authentication attempts.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AdaptiveThreatProtectionAdapter implements SecurityThreatProtectionPort {

  private record AttemptKey(String principal, String deviceId, String ipAddress) {}

  private record BlacklistEntry(String reason) {}

  private final ThreatProtectionProperties threatProtectionProperties;

  private final ConcurrentMap<AttemptKey, Integer> loginFailures = new ConcurrentHashMap<>();
  private final ConcurrentMap<AttemptKey, Integer> suspiciousRequests = new ConcurrentHashMap<>();
  private final ExpiringMap<String, BlacklistEntry> blacklist =
      ExpiringMap.builder().variableExpiration().maxSize(20_000).build();

  /**
   * Verifies whether a login attempt should be blocked before credentials are even processed. The
   * check evaluates blacklist entries derived from principal, device and IP so security decisions
   * can stop abusive callers early in the authentication pipeline.
   */
  @Override
  public void assertLoginAllowed(
      final String email, final String deviceId, final RequestMetadata requestMetadata) {
    assertNotBlacklisted(email, deviceId, requestMetadata);
  }

  /**
   * Records a failed login against the aggregated attempt counters maintained by the adapter. Once
   * the configured threshold is exceeded, the method converts the accumulated failures into
   * blacklist entries and clears the transient counter for that request context.
   */
  @Override
  public void recordLoginFailure(
      final String email, final String deviceId, final RequestMetadata requestMetadata) {
    final AttemptKey attemptKey = attemptKey(email, deviceId, requestMetadata);
    final int failures = loginFailures.merge(attemptKey, 1, Integer::sum);
    if (failures >= threatProtectionProperties.failedLoginBlacklistThreshold()) {
      blacklist(email, deviceId, requestMetadata, "failed_login_threshold");
      loginFailures.remove(attemptKey);
    }
  }

  /**
   * Clears the transient failed-login counters after the caller authenticates successfully. This
   * prevents earlier mistakes from continuing to influence future blacklist decisions for the same
   * principal, device and IP combination.
   */
  @Override
  public void recordLoginSuccess(
      final String email, final String deviceId, final RequestMetadata requestMetadata) {
    loginFailures.remove(attemptKey(email, deviceId, requestMetadata));
  }

  /**
   * Applies the same blacklist policy to already authenticated requests that carry an identity. The
   * method is called from JWT validation paths so blacklisted callers cannot keep using a token
   * that would otherwise pass cryptographic validation.
   */
  @Override
  public void assertRequestAllowed(
      final String principal, final String deviceId, final RequestMetadata requestMetadata) {
    assertNotBlacklisted(principal, deviceId, requestMetadata);
  }

  /**
   * Records a suspicious request signal such as device mismatch or unexpected fingerprint state.
   * Depending on policy, the anomaly may immediately create blacklist entries or accumulate until
   * the configured suspicious-request threshold has been reached for that caller context.
   */
  @Override
  public void recordSuspiciousRequest(
      final String principal,
      final String deviceId,
      final RequestMetadata requestMetadata,
      final String reason) {
    final AttemptKey attemptKey = attemptKey(principal, deviceId, requestMetadata);
    final int suspiciousCount = suspiciousRequests.merge(attemptKey, 1, Integer::sum);
    final boolean shouldBlacklist =
        threatProtectionProperties.immediateBlacklistOnDeviceMismatch()
            || suspiciousCount >= threatProtectionProperties.suspiciousRequestThreshold();
    if (shouldBlacklist) {
      blacklist(principal, deviceId, requestMetadata, reason);
      suspiciousRequests.remove(attemptKey);
    }
  }

  /**
   * Verifies whether any blacklist key relevant to the current request is still active. The method
   * checks principal, device, IP and combined keys so the block can be enforced even when only one
   * of those dimensions was sufficient to classify earlier traffic as malicious.
   */
  private void assertNotBlacklisted(
      final String principal, final String deviceId, final RequestMetadata requestMetadata) {
    for (String key : blacklistKeys(principal, deviceId, requestMetadata.ipAddress())) {
      if (blacklist.containsKey(key)) {
        log.atWarn()
            .addKeyValue("event", "security.blacklist.blocked")
            .addKeyValue("principal", principal)
            .addKeyValue("deviceId", deviceId)
            .addKeyValue("ipAddress", requestMetadata.ipAddress())
            .addKeyValue("blacklistKey", key)
            .log("security.blacklist.blocked");
        throw new AppException("security.blacklist.error.blocked", HttpStatus.FORBIDDEN);
      }
    }
  }

  /**
   * Creates blacklist entries for every dimension used by request evaluation. By persisting
   * principal, device, IP and combined keys together, later checks can stop related traffic even
   * when one of the identifying values changes between requests.
   */
  private void blacklist(
      final String principal,
      final String deviceId,
      final RequestMetadata requestMetadata,
      final String reason) {
    for (String key : blacklistKeys(principal, deviceId, requestMetadata.ipAddress())) {
      blacklist.put(
          key,
          new BlacklistEntry(reason),
          threatProtectionProperties.blacklistSeconds(),
          TimeUnit.SECONDS);
    }
    log.atWarn()
        .addKeyValue("event", "security.blacklist.created")
        .addKeyValue("principal", principal)
        .addKeyValue("deviceId", deviceId)
        .addKeyValue("ipAddress", requestMetadata.ipAddress())
        .addKeyValue("reason", reason)
        .addKeyValue("ttlSeconds", threatProtectionProperties.blacklistSeconds())
        .log("security.blacklist.created");
  }

  /**
   * Normalizes principal, device and IP data before they are used as aggregation keys. Consistent
   * normalization ensures counters and blacklist decisions are not fragmented by casing, blanks or
   * missing values that actually refer to the same caller context.
   */
  private AttemptKey attemptKey(
      final String principal, final String deviceId, final RequestMetadata requestMetadata) {
    return new AttemptKey(
        StringUtils.defaultIfBlank(principal, "anonymous").trim().toLowerCase(),
        StringUtils.defaultIfBlank(deviceId, "unknown"),
        StringUtils.defaultIfBlank(requestMetadata.ipAddress(), "unknown"));
  }

  /**
   * Builds the set of blacklist keys that can be used to block the current request. These keys
   * represent the same caller from different perspectives so later checks can enforce security even
   * when only part of the identifying context matches a previous suspicious event.
   */
  private List<String> blacklistKeys(
      final String principal, final String deviceId, final String ipAddress) {
    return List.of(
        "principal:" + StringUtils.defaultIfBlank(principal, "anonymous").trim().toLowerCase(),
        "device:" + StringUtils.defaultIfBlank(deviceId, "unknown"),
        "ip:" + StringUtils.defaultIfBlank(ipAddress, "unknown"),
        "combo:"
            + StringUtils.defaultIfBlank(principal, "anonymous").trim().toLowerCase()
            + '|'
            + StringUtils.defaultIfBlank(deviceId, "unknown")
            + '|'
            + StringUtils.defaultIfBlank(ipAddress, "unknown"));
  }
}
