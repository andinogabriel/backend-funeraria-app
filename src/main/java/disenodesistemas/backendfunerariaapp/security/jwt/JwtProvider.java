package disenodesistemas.backendfunerariaapp.security.jwt;

import disenodesistemas.backendfunerariaapp.cache.LoggedOutJwtTokenCache;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.event.OnUserLogoutSuccessEvent;
import disenodesistemas.backendfunerariaapp.exception.InvalidTokenRequestException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Encapsulates JWT creation, parsing and validation for access tokens issued by the application.
 * The provider embeds device identity, fingerprint and token-version claims so downstream filters
 * can verify that a bearer token still belongs to the active persisted session that created it.
 */
@Slf4j
@Component
public class JwtProvider {

  private final JwtProperties jwtProperties;
  private final LoggedOutJwtTokenCache loggedOutJwtTokenCache;

  /**
   * Creates the JWT provider with the configured token properties and the logout cache used for
   * immediate invalidation. The logout cache is injected lazily to avoid circular initialization
   * while still letting token parsing consult recent logout events.
   */
  public JwtProvider(
      final JwtProperties jwtProperties,
      @Lazy final LoggedOutJwtTokenCache loggedOutJwtTokenCache) {
    this.jwtProperties = jwtProperties;
    this.loggedOutJwtTokenCache = loggedOutJwtTokenCache;
  }

  /**
   * Creates a signed access token that embeds authorities and device-binding claims for the
   * authenticated session. Those claims allow downstream filters to confirm not only signature
   * integrity but also whether the token still belongs to the active persisted device session.
   */
  public String generateAccessToken(final UserEntity user, final UserDevice userDevice) {
    final String authoritiesGranted =
        user.getRoles().stream()
            .map(role -> role.getName().name())
            .collect(java.util.stream.Collectors.joining(","));

    final Instant now = Instant.now();
    final Instant exp = now.plusSeconds(jwtProperties.expirationSeconds());

    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject(user.getEmail())
        .claim(jwtProperties.authorities(), authoritiesGranted)
        .claim(jwtProperties.deviceIdClaim(), userDevice.getDeviceId())
        .claim(jwtProperties.deviceFingerprintClaim(), userDevice.getFingerprintHash())
        .claim(jwtProperties.deviceVersionClaim(), userDevice.getTokenVersion())
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(signingKey(), Jwts.SIG.HS512)
        .compact();
  }

  /**
   * Parses the token claims after verifying signature integrity and logout invalidation state. The
   * method is the main entry point for token inspection because it rejects logged-out tokens before
   * returning any claim payload to the caller.
   */
  public Claims parseClaims(final String token) {
    validateTokenIsNotForALoggedOutDevice(token);

    return Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload();
  }

  /**
   * Validates a token end-to-end without requiring the caller to inspect individual claims. It is
   * effectively a convenience wrapper around full parsing and is useful in tests or guards that
   * only need to know whether the token is currently acceptable.
   */
  public boolean validateToken(final String token) {
    parseClaims(token);
    return true;
  }

  /**
   * Returns the subject claim used as the authenticated principal in the security layer. The value
   * is resolved through full token parsing so logout invalidation and signature verification still
   * apply before the subject is exposed to callers.
   */
  public String getUserNameFromToken(final String token) {
    return parseClaims(token).getSubject();
  }

  /**
   * Returns the expiration timestamp declared by the JWT. The value is obtained after full token
   * validation so callers can safely use it for TTL calculations such as logout-marker caching or
   * diagnostics around token lifetime.
   */
  public Date getTokenExpiryFromJWT(final String token) {
    return parseClaims(token).getExpiration();
  }

  /**
   * Returns the configured access-token lifetime in milliseconds as exposed to clients. The method
   * keeps response-building code independent from configuration math and ensures a single source of
   * truth for token duration communicated by the API.
   */
  public long getExpiryDuration() {
    return jwtProperties.expirationSeconds() * 1000L;
  }

  /**
   * Extracts the serialized authorities claim into a normalized list of role names. The method
   * tolerates missing or blank claim values so callers can consistently map authorities without
   * repeating CSV parsing and trimming logic.
   */
  public List<String> extractAuthorities(final Claims claims) {
    final Object raw = claims.get(jwtProperties.authorities());
    if (raw == null) {
      return List.of();
    }

    final String csv = raw.toString();
    if (StringUtils.isBlank(csv)) {
      return List.of();
    }

    return Stream.of(csv.split(",")).map(String::trim).filter(StringUtils::isNotBlank).toList();
  }

  /**
   * Returns the device identifier claim embedded in the JWT. This value is later compared with the
   * inbound `X-Device-Id` header and the persisted device session to detect replay from another
   * client context.
   */
  public String extractDeviceId(final Claims claims) {
    return claims.get(jwtProperties.deviceIdClaim(), String.class);
  }

  /**
   * Returns the device fingerprint claim embedded in the JWT. The claim is used together with the
   * current request metadata to verify that the token still represents the same device and
   * user-agent combination that created it.
   */
  public String extractDeviceFingerprint(final Claims claims) {
    return claims.get(jwtProperties.deviceFingerprintClaim(), String.class);
  }

  /**
   * Returns the token-version claim carried by the JWT. The version is compared with the persisted
   * device session so tokens issued before logout, refresh or session re-registration can be
   * rejected as stale even when the signature is still valid.
   */
  public Long extractDeviceVersion(final Claims claims) {
    final Number rawVersion = claims.get(jwtProperties.deviceVersionClaim(), Number.class);
    return rawVersion == null ? null : rawVersion.longValue();
  }

  /**
   * Builds the HMAC signing key used both for JWT creation and signature verification. Centralizing
   * key construction ensures generation and parsing always rely on the same secret material and
   * algorithm expectations.
   */
  private SecretKey signingKey() {
    final byte[] bytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(bytes);
  }

  /**
   * Rejects access tokens that were already marked as logged out in the short-lived cache. This is
   * what allows the application to invalidate an access token immediately after logout instead of
   * waiting until the original JWT expiration time has elapsed.
   */
  private void validateTokenIsNotForALoggedOutDevice(final String authToken) {
    final OnUserLogoutSuccessEvent logoutEvent =
        loggedOutJwtTokenCache.getLogoutEventForToken(authToken);

    if (logoutEvent == null) {
      return;
    }

    log.atWarn()
        .addKeyValue("event", "security.jwt.logged_out_token_detected")
        .addKeyValue("email", logoutEvent.userEmail())
        .addKeyValue("logoutAt", logoutEvent.eventTime())
        .log("security.jwt.logged_out_token_detected");
    throw new InvalidTokenRequestException("JWT", authToken, "jwt.token.error.logged.out");
  }
}
