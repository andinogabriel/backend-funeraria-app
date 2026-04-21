package disenodesistemas.backendfunerariaapp.security.jwt;

import disenodesistemas.backendfunerariaapp.application.model.RequestMetadata;
import disenodesistemas.backendfunerariaapp.application.port.out.DeviceFingerprintPort;
import disenodesistemas.backendfunerariaapp.application.port.out.MessageResolverPort;
import disenodesistemas.backendfunerariaapp.application.port.out.SecurityThreatProtectionPort;
import disenodesistemas.backendfunerariaapp.application.port.out.UserDevicePort;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.exception.InvalidTokenRequestException;
import disenodesistemas.backendfunerariaapp.security.request.SecurityRequestProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Validates bearer tokens on incoming requests before protected endpoints are executed. Besides
 * verifying JWT integrity, it enforces device binding, persisted session continuity and adaptive
 * threat checks so stolen or stale tokens can be rejected early in the security chain.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

  private final JwtProperties jwtProperties;
  private final JwtProvider jwtProvider;
  private final MessageResolverPort messageResolverPort;
  private final DeviceFingerprintPort deviceFingerprintPort;
  private final SecurityThreatProtectionPort securityThreatProtectionPort;
  private final UserDevicePort userDevicePort;
  private final SecurityRequestProperties securityRequestProperties;

  /**
   * Resolves the bearer token, authenticates the request when present and translates token
   * failures into localized HTTP responses. This method is the top-level security guard for every
   * protected request handled by the JWT authentication chain.
   */
  @Override
  protected void doFilterInternal(
      final @NonNull HttpServletRequest request,
      final @NonNull HttpServletResponse response,
      final @NonNull FilterChain chain)
      throws ServletException, IOException {
    try {
      final String token = resolveToken(request);
      if (StringUtils.isBlank(token)) {
        continueUnauthenticated(request, response, chain);
        return;
      }

      authenticateRequest(token, request);
      chain.doFilter(request, response);
    } catch (ExpiredJwtException ex) {
      handleUnauthorized(request, response, "security.jwt.expired", ex.getMessage(),
          "jwt.token.error.expired.exception");
    } catch (UnsupportedJwtException ex) {
      handleUnauthorized(request, response, "security.jwt.unsupported", ex.getMessage(),
          "jwt.token.error.unsupported.exception");
    } catch (MalformedJwtException ex) {
      handleUnauthorized(request, response, "security.jwt.malformed", ex.getMessage(),
          "jwt.token.error.malFormed.exception");
    } catch (SignatureException ex) {
      handleUnauthorized(request, response, "security.jwt.signature_invalid", ex.getMessage(),
          "jwt.token.error.signature.exception");
    } catch (InvalidTokenRequestException ex) {
      handleInvalidTokenRequest(request, response, ex);
    } catch (AppException ex) {
      handleApplicationError(request, response, ex);
    } catch (IllegalArgumentException ex) {
      handleUnauthorized(request, response, "security.jwt.invalid_input", ex.getMessage(),
          "jwt.token.error.illegalArgument.exception");
    } catch (JwtException ex) {
      handleUnauthorized(request, response, "security.jwt.invalid", ex.getMessage(),
          "jwt.token.error.illegalArgument.exception");
    }
  }

  /**
   * Clears the current security context and continues the chain when no bearer token was supplied.
   * This preserves stateless behavior for public endpoints while ensuring no stale authentication
   * information leaks from previous processing on the same thread.
   */
  private void continueUnauthenticated(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain chain)
      throws ServletException, IOException {
    SecurityContextHolder.clearContext();
    chain.doFilter(request, response);
  }

  /**
   * Parses the JWT, evaluates request-level threat checks and validates device continuity before
   * publishing the authenticated principal. The method is the central bridge between raw bearer
   * token parsing and Spring Security context registration.
   */
  private void authenticateRequest(final String token, final HttpServletRequest request) {
    final Claims claims = jwtProvider.parseClaims(token);
    final RequestMetadata requestMetadata = requestMetadata(request);
    final String principal = claims.getSubject();
    final String tokenDeviceId = jwtProvider.extractDeviceId(claims);

    securityThreatProtectionPort.assertRequestAllowed(principal, tokenDeviceId, requestMetadata);
    validateDeviceBinding(token, claims, requestMetadata);
    registerAuthentication(principal, claims);
  }

  /**
   * Publishes the authenticated principal and granted authorities into Spring Security's context.
   * Once this method succeeds, the rest of the request lifecycle can rely on a fully populated
   * authentication object for authorization and user-resolution concerns.
   */
  private void registerAuthentication(final String principal, final Claims claims) {
    final Collection<SimpleGrantedAuthority> authorities =
        jwtProvider.extractAuthorities(claims).stream().map(SimpleGrantedAuthority::new).toList();

    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, authorities));
  }

  /**
   * Sends a localized HTTP 401 response for low-level JWT parsing and signature failures. The
   * method also clears the security context and emits a structured warning log that captures the
   * failure category and the request metadata relevant to troubleshooting.
   */
  private void handleUnauthorized(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final String event,
      final String detail,
      final String messageKey)
      throws IOException {
    SecurityContextHolder.clearContext();
    logJwtWarning(event, request).addKeyValue("detail", detail).log(event);
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, messageResolverPort.getMessage(messageKey));
  }

  /**
   * Sends a localized HTTP 401 response for domain-specific token validation failures such as
   * device mismatch or stale session version. These cases happen after the token is structurally
   * valid but no longer acceptable according to business security rules.
   */
  private void handleInvalidTokenRequest(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final InvalidTokenRequestException exception)
      throws IOException {
    SecurityContextHolder.clearContext();
    logJwtWarning("security.jwt.rejected", request)
        .addKeyValue("reason", exception.getMessage())
        .addKeyValue("tokenType", exception.getTokenType())
        .log("security.jwt.rejected");
    response.sendError(
        HttpServletResponse.SC_UNAUTHORIZED,
        messageResolverPort.getMessage(exception.getMessage()));
  }

  /**
   * Sends the HTTP response dictated by an application-level security exception. This path is used
   * when adaptive protections such as blacklist checks decide to block the request with a status
   * other than the default JWT unauthorized response.
   */
  private void handleApplicationError(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final AppException exception)
      throws IOException {
    SecurityContextHolder.clearContext();
    logJwtWarning("security.jwt.blocked", request)
        .addKeyValue("reason", exception.getMessage())
        .addKeyValue("status", exception.getStatus().value())
        .log("security.jwt.blocked");
    response.sendError(
        exception.getStatus().value(), messageResolverPort.getMessage(exception.getMessage()));
  }

  /**
   * Ensures that the token still belongs to the same device id, fingerprint and persisted device
   * session version. This is the main anti-theft check because it correlates the bearer token with
   * both the inbound request metadata and the server-side device session state.
   */
  private void validateDeviceBinding(
      final String token, final Claims claims, final RequestMetadata requestMetadata) {
    final String requestDeviceId = requestMetadata.deviceIdHeader();
    if (StringUtils.isBlank(requestDeviceId)) {
      securityThreatProtectionPort.recordSuspiciousRequest(
          claims.getSubject(), null, requestMetadata, "missing_device_header");
      throw new InvalidTokenRequestException("JWT", token, "security.request.device.header.required");
    }

    final String tokenDeviceId = jwtProvider.extractDeviceId(claims);
    if (!Strings.CS.equals(tokenDeviceId, requestDeviceId)) {
      securityThreatProtectionPort.recordSuspiciousRequest(
          claims.getSubject(), requestDeviceId, requestMetadata, "device_id_mismatch");
      throw new InvalidTokenRequestException("JWT", token, "security.jwt.device.mismatch");
    }

    final String requestFingerprint =
        deviceFingerprintPort.fingerprint(requestDeviceId, requestMetadata.userAgent());
    final String tokenFingerprint = jwtProvider.extractDeviceFingerprint(claims);
    if (!Strings.CS.equals(tokenFingerprint, requestFingerprint)) {
      securityThreatProtectionPort.recordSuspiciousRequest(
          claims.getSubject(), requestDeviceId, requestMetadata, "device_fingerprint_mismatch");
      throw new InvalidTokenRequestException("JWT", token, "security.jwt.device.mismatch");
    }

    final UserDevice userDevice = resolveRegisteredDevice(token, claims, tokenDeviceId);
    validatePersistedDeviceState(token, claims, requestMetadata, userDevice, tokenFingerprint);
  }

  /**
   * Resolves the persisted device session that should own the incoming JWT. Loading the server-side
   * session is required so the filter can compare token claims against the latest stored
   * fingerprint, refresh state and token version for that device.
   */
  private UserDevice resolveRegisteredDevice(
      final String token, final Claims claims, final String tokenDeviceId) {
    return userDevicePort
        .findByUserEmailAndDeviceId(claims.getSubject(), tokenDeviceId)
        .orElseThrow(
            () -> new InvalidTokenRequestException("JWT", token, "security.jwt.device.unregistered"));
  }

  /**
   * Validates the persisted device-session state associated with the token. The method confirms the
   * session is still active, the stored fingerprint still matches and the token version has not
   * become stale due to logout, refresh or device re-registration.
   */
  private void validatePersistedDeviceState(
      final String token,
      final Claims claims,
      final RequestMetadata requestMetadata,
      final UserDevice userDevice,
      final String tokenFingerprint) {
    if (Boolean.FALSE.equals(userDevice.getIsRefreshActive())) {
      throw new InvalidTokenRequestException("JWT", token, "security.jwt.session.inactive");
    }

    final String requestDeviceId = requestMetadata.deviceIdHeader();
    if (!Strings.CS.equals(userDevice.getFingerprintHash(), tokenFingerprint)) {
      securityThreatProtectionPort.recordSuspiciousRequest(
          claims.getSubject(), requestDeviceId, requestMetadata, "stored_device_fingerprint_mismatch");
      throw new InvalidTokenRequestException("JWT", token, "security.jwt.device.mismatch");
    }

    final Long tokenVersion = jwtProvider.extractDeviceVersion(claims);
    if (tokenVersion == null || !tokenVersion.equals(userDevice.getTokenVersion())) {
      throw new InvalidTokenRequestException("JWT", token, "security.jwt.session.stale");
    }
  }

  /**
   * Builds the normalized request metadata needed by device-binding and threat-protection checks.
   * Normalization here keeps JWT validation independent from direct servlet access deeper in the
   * call graph and guarantees stable fallback values for missing headers.
   */
  private RequestMetadata requestMetadata(final HttpServletRequest request) {
    return new RequestMetadata(
        StringUtils.defaultIfBlank(request.getRemoteAddr(), "unknown"),
        StringUtils.defaultIfBlank(request.getHeader("User-Agent"), "unknown"),
        StringUtils.trimToNull(request.getHeader(securityRequestProperties.deviceIdHeader())),
        StringUtils.trimToNull(request.getHeader(securityRequestProperties.idempotencyKeyHeader())));
  }

  /**
   * Extracts the bearer token from the configured Authorization header if the expected prefix is
   * present. Requests with missing headers or malformed prefixes are treated as unauthenticated
   * rather than immediately erroneous at this stage of the filter.
   */
  private String resolveToken(final HttpServletRequest request) {
    final String headerName =
        StringUtils.defaultIfBlank(jwtProperties.header(), HttpHeaders.AUTHORIZATION);
    final String authHeader = request.getHeader(headerName);
    if (StringUtils.isBlank(authHeader)) {
      return null;
    }

    final String prefix = StringUtils.defaultIfBlank(jwtProperties.prefix(), "Bearer");
    final String expected = prefix + StringUtils.SPACE;
    if (!Strings.CI.startsWith(authHeader, expected)) {
      return null;
    }

    return StringUtils.trimToNull(StringUtils.substringAfter(authHeader, expected));
  }

  /**
   * Prepares the structured log builder shared by all JWT rejection paths in this filter. Reusing
   * the builder setup keeps security logs consistent across different failure branches and reduces
   * duplication in the error-handling methods above.
   */
  private LoggingEventBuilder logJwtWarning(
      final String event, final HttpServletRequest request) {
    return log.atWarn()
        .addKeyValue("event", event)
        .addKeyValue("method", StringUtils.defaultIfBlank(request.getMethod(), "UNKNOWN"))
        .addKeyValue("path", StringUtils.defaultIfBlank(request.getRequestURI(), "/"))
        .addKeyValue(
            "deviceIdHeader",
            StringUtils.defaultIfBlank(
                request.getHeader(securityRequestProperties.deviceIdHeader()), "missing"));
  }
}
