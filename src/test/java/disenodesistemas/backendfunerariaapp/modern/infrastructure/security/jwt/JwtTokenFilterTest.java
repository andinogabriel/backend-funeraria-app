package disenodesistemas.backendfunerariaapp.modern.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.DeviceFingerprintPort;
import disenodesistemas.backendfunerariaapp.application.port.out.MessageResolverPort;
import disenodesistemas.backendfunerariaapp.application.port.out.SecurityThreatProtectionPort;
import disenodesistemas.backendfunerariaapp.application.port.out.UserDevicePort;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import disenodesistemas.backendfunerariaapp.modern.support.TestValues;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.JwtProperties;
import disenodesistemas.backendfunerariaapp.infrastructure.security.jwt.JwtProvider;
import disenodesistemas.backendfunerariaapp.infrastructure.security.jwt.JwtTokenFilter;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.SecurityRequestProperties;
import io.jsonwebtoken.Claims;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenFilter")
class JwtTokenFilterTest {

  @Mock private JwtProvider jwtProvider;
  @Mock private MessageResolverPort messageResolverPort;
  @Mock private DeviceFingerprintPort deviceFingerprintPort;
  @Mock private SecurityThreatProtectionPort securityThreatProtectionPort;
  @Mock private UserDevicePort userDevicePort;

  private final JwtProperties jwtProperties = SecurityTestDataFactory.jwtProperties();

  private final SecurityRequestProperties securityRequestProperties =
      SecurityTestDataFactory.securityRequestProperties();

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName(
      "Given a valid bearer token bound to the same device when the filter is executed then it authenticates the request")
  void givenAValidBearerTokenBoundToTheSameDeviceWhenTheFilterIsExecutedThenItAuthenticatesTheRequest()
      throws Exception {
    final JwtTokenFilter filter =
        new JwtTokenFilter(
            jwtProperties,
            jwtProvider,
            messageResolverPort,
            deviceFingerprintPort,
            securityThreatProtectionPort,
            userDevicePort,
            securityRequestProperties);
    final Claims claims = mock(Claims.class);
    final UserDevice userDevice =
        SecurityTestDataFactory.userDevice(SecurityTestDataFactory.userEntity());
    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(TestValues.AUTHORIZATION_HEADER, "Bearer access-token");
    request.addHeader(TestValues.DEVICE_ID_HEADER, TestValues.DEVICE_ID);
    request.addHeader("User-Agent", TestValues.USER_AGENT);
    final MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtProvider.parseClaims("access-token")).thenReturn(claims);
    when(claims.getSubject()).thenReturn(TestValues.USER_EMAIL);
    when(jwtProvider.extractDeviceId(claims)).thenReturn(TestValues.DEVICE_ID);
    when(jwtProvider.extractDeviceFingerprint(claims)).thenReturn("fingerprint-hash");
    when(jwtProvider.extractDeviceVersion(claims)).thenReturn(2L);
    when(jwtProvider.extractAuthorities(claims)).thenReturn(List.of("ROLE_USER"));
    when(deviceFingerprintPort.fingerprint(TestValues.DEVICE_ID, TestValues.USER_AGENT))
        .thenReturn("fingerprint-hash");
    when(userDevicePort.findByUserEmailAndDeviceId(TestValues.USER_EMAIL, TestValues.DEVICE_ID))
        .thenReturn(Optional.of(userDevice));

    filter.doFilter(request, response, new MockFilterChain());

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
        .isEqualTo(TestValues.USER_EMAIL);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  @DisplayName(
      "Given a bearer token presented from a different device when the filter is executed then it rejects the request and records the suspicious activity")
  void givenABearerTokenPresentedFromADifferentDeviceWhenTheFilterIsExecutedThenItRejectsTheRequestAndRecordsTheSuspiciousActivity()
      throws Exception {
    final JwtTokenFilter filter =
        new JwtTokenFilter(
            jwtProperties,
            jwtProvider,
            messageResolverPort,
            deviceFingerprintPort,
            securityThreatProtectionPort,
            userDevicePort,
            securityRequestProperties);
    final Claims claims = mock(Claims.class);
    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(TestValues.AUTHORIZATION_HEADER, "Bearer access-token");
    request.addHeader(TestValues.DEVICE_ID_HEADER, TestValues.ALTERNATE_DEVICE_ID);
    request.addHeader("User-Agent", TestValues.USER_AGENT);
    final MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtProvider.parseClaims("access-token")).thenReturn(claims);
    when(claims.getSubject()).thenReturn(TestValues.USER_EMAIL);
    when(jwtProvider.extractDeviceId(claims)).thenReturn(TestValues.DEVICE_ID);
    when(messageResolverPort.getMessage("security.jwt.device.mismatch"))
        .thenReturn("El token no coincide con el dispositivo actual.");

    filter.doFilter(request, response, new MockFilterChain());

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    assertThat(response.getStatus()).isEqualTo(401);
    verify(securityThreatProtectionPort)
        .recordSuspiciousRequest(
            org.mockito.Mockito.eq(TestValues.USER_EMAIL),
            org.mockito.Mockito.eq(TestValues.ALTERNATE_DEVICE_ID),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.Mockito.eq("device_id_mismatch"));
  }

  @Test
  @DisplayName(
      "Given a request without a bearer token when the filter is executed then it leaves the request unauthenticated and continues the chain")
  void givenARequestWithoutABearerTokenWhenTheFilterIsExecutedThenItLeavesTheRequestUnauthenticatedAndContinuesTheChain()
      throws Exception {
    final JwtTokenFilter filter =
        new JwtTokenFilter(
            jwtProperties,
            jwtProvider,
            messageResolverPort,
            deviceFingerprintPort,
            securityThreatProtectionPort,
            userDevicePort,
            securityRequestProperties);
    final MockHttpServletRequest request = new MockHttpServletRequest();
    final MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new MockFilterChain());

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  @DisplayName(
      "Given a bearer token without the device header when the filter is executed then it rejects the request and records the suspicious activity")
  void givenABearerTokenWithoutTheDeviceHeaderWhenTheFilterIsExecutedThenItRejectsTheRequestAndRecordsTheSuspiciousActivity()
      throws Exception {
    final JwtTokenFilter filter =
        new JwtTokenFilter(
            jwtProperties,
            jwtProvider,
            messageResolverPort,
            deviceFingerprintPort,
            securityThreatProtectionPort,
            userDevicePort,
            securityRequestProperties);
    final Claims claims = mock(Claims.class);
    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(TestValues.AUTHORIZATION_HEADER, "Bearer access-token");
    request.addHeader("User-Agent", TestValues.USER_AGENT);
    final MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtProvider.parseClaims("access-token")).thenReturn(claims);
    when(claims.getSubject()).thenReturn(TestValues.USER_EMAIL);
    when(jwtProvider.extractDeviceId(claims)).thenReturn(TestValues.DEVICE_ID);
    when(messageResolverPort.getMessage("security.request.device.header.required"))
        .thenReturn("El header del dispositivo es obligatorio.");

    filter.doFilter(request, response, new MockFilterChain());

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getErrorMessage()).isEqualTo("El header del dispositivo es obligatorio.");
    verify(securityThreatProtectionPort)
        .recordSuspiciousRequest(
            org.mockito.Mockito.eq(TestValues.USER_EMAIL),
            org.mockito.Mockito.isNull(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.Mockito.eq("missing_device_header"));
  }

  @Test
  @DisplayName(
      "Given a bearer token with a mismatched device fingerprint when the filter is executed then it rejects the request and records the mismatch")
  void givenABearerTokenWithAMismatchedDeviceFingerprintWhenTheFilterIsExecutedThenItRejectsTheRequestAndRecordsTheMismatch()
      throws Exception {
    final JwtTokenFilter filter =
        new JwtTokenFilter(
            jwtProperties,
            jwtProvider,
            messageResolverPort,
            deviceFingerprintPort,
            securityThreatProtectionPort,
            userDevicePort,
            securityRequestProperties);
    final Claims claims = mock(Claims.class);
    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(TestValues.AUTHORIZATION_HEADER, "Bearer access-token");
    request.addHeader(TestValues.DEVICE_ID_HEADER, TestValues.DEVICE_ID);
    request.addHeader("User-Agent", TestValues.USER_AGENT);
    final MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtProvider.parseClaims("access-token")).thenReturn(claims);
    when(claims.getSubject()).thenReturn(TestValues.USER_EMAIL);
    when(jwtProvider.extractDeviceId(claims)).thenReturn(TestValues.DEVICE_ID);
    when(jwtProvider.extractDeviceFingerprint(claims)).thenReturn("token-fingerprint");
    when(deviceFingerprintPort.fingerprint(TestValues.DEVICE_ID, TestValues.USER_AGENT))
        .thenReturn("request-fingerprint");
    when(messageResolverPort.getMessage("security.jwt.device.mismatch"))
        .thenReturn("El token no coincide con el dispositivo actual.");

    filter.doFilter(request, response, new MockFilterChain());

    assertThat(response.getStatus()).isEqualTo(401);
    verify(securityThreatProtectionPort)
        .recordSuspiciousRequest(
            org.mockito.Mockito.eq(TestValues.USER_EMAIL),
            org.mockito.Mockito.eq(TestValues.DEVICE_ID),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.Mockito.eq("device_fingerprint_mismatch"));
  }

  @Test
  @DisplayName(
      "Given a bearer token for an unregistered device when the filter is executed then it rejects the request with the localized message")
  void givenABearerTokenForAnUnregisteredDeviceWhenTheFilterIsExecutedThenItRejectsTheRequestWithTheLocalizedMessage()
      throws Exception {
    final JwtTokenFilter filter =
        new JwtTokenFilter(
            jwtProperties,
            jwtProvider,
            messageResolverPort,
            deviceFingerprintPort,
            securityThreatProtectionPort,
            userDevicePort,
            securityRequestProperties);
    final Claims claims = mock(Claims.class);
    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(TestValues.AUTHORIZATION_HEADER, "Bearer access-token");
    request.addHeader(TestValues.DEVICE_ID_HEADER, TestValues.DEVICE_ID);
    request.addHeader("User-Agent", TestValues.USER_AGENT);
    final MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtProvider.parseClaims("access-token")).thenReturn(claims);
    when(claims.getSubject()).thenReturn(TestValues.USER_EMAIL);
    when(jwtProvider.extractDeviceId(claims)).thenReturn(TestValues.DEVICE_ID);
    when(jwtProvider.extractDeviceFingerprint(claims)).thenReturn("fingerprint-hash");
    when(deviceFingerprintPort.fingerprint(TestValues.DEVICE_ID, TestValues.USER_AGENT))
        .thenReturn("fingerprint-hash");
    when(userDevicePort.findByUserEmailAndDeviceId(TestValues.USER_EMAIL, TestValues.DEVICE_ID))
        .thenReturn(Optional.empty());
    when(messageResolverPort.getMessage("security.jwt.device.unregistered"))
        .thenReturn("El dispositivo no esta registrado.");

    filter.doFilter(request, response, new MockFilterChain());

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getErrorMessage()).isEqualTo("El dispositivo no esta registrado.");
  }

  @Test
  @DisplayName(
      "Given a bearer token for an inactive device session when the filter is executed then it rejects the request as an inactive session")
  void givenABearerTokenForAnInactiveDeviceSessionWhenTheFilterIsExecutedThenItRejectsTheRequestAsAnInactiveSession()
      throws Exception {
    final JwtTokenFilter filter =
        new JwtTokenFilter(
            jwtProperties,
            jwtProvider,
            messageResolverPort,
            deviceFingerprintPort,
            securityThreatProtectionPort,
            userDevicePort,
            securityRequestProperties);
    final Claims claims = mock(Claims.class);
    final UserDevice userDevice =
        SecurityTestDataFactory.userDevice(SecurityTestDataFactory.userEntity());
    userDevice.setIsRefreshActive(Boolean.FALSE);
    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(TestValues.AUTHORIZATION_HEADER, "Bearer access-token");
    request.addHeader(TestValues.DEVICE_ID_HEADER, TestValues.DEVICE_ID);
    request.addHeader("User-Agent", TestValues.USER_AGENT);
    final MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtProvider.parseClaims("access-token")).thenReturn(claims);
    when(claims.getSubject()).thenReturn(TestValues.USER_EMAIL);
    when(jwtProvider.extractDeviceId(claims)).thenReturn(TestValues.DEVICE_ID);
    when(jwtProvider.extractDeviceFingerprint(claims)).thenReturn("fingerprint-hash");
    when(deviceFingerprintPort.fingerprint(TestValues.DEVICE_ID, TestValues.USER_AGENT))
        .thenReturn("fingerprint-hash");
    when(userDevicePort.findByUserEmailAndDeviceId(TestValues.USER_EMAIL, TestValues.DEVICE_ID))
        .thenReturn(Optional.of(userDevice));
    when(messageResolverPort.getMessage("security.jwt.session.inactive"))
        .thenReturn("La sesion del dispositivo esta inactiva.");

    filter.doFilter(request, response, new MockFilterChain());

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getErrorMessage()).isEqualTo("La sesion del dispositivo esta inactiva.");
  }

  @Test
  @DisplayName(
      "Given a bearer token with a stale token version when the filter is executed then it rejects the request as a stale session")
  void givenABearerTokenWithAStaleTokenVersionWhenTheFilterIsExecutedThenItRejectsTheRequestAsAStaleSession()
      throws Exception {
    final JwtTokenFilter filter =
        new JwtTokenFilter(
            jwtProperties,
            jwtProvider,
            messageResolverPort,
            deviceFingerprintPort,
            securityThreatProtectionPort,
            userDevicePort,
            securityRequestProperties);
    final Claims claims = mock(Claims.class);
    final UserDevice userDevice =
        SecurityTestDataFactory.userDevice(SecurityTestDataFactory.userEntity());
    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(TestValues.AUTHORIZATION_HEADER, "Bearer access-token");
    request.addHeader(TestValues.DEVICE_ID_HEADER, TestValues.DEVICE_ID);
    request.addHeader("User-Agent", TestValues.USER_AGENT);
    final MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtProvider.parseClaims("access-token")).thenReturn(claims);
    when(claims.getSubject()).thenReturn(TestValues.USER_EMAIL);
    when(jwtProvider.extractDeviceId(claims)).thenReturn(TestValues.DEVICE_ID);
    when(jwtProvider.extractDeviceFingerprint(claims)).thenReturn("fingerprint-hash");
    when(jwtProvider.extractDeviceVersion(claims)).thenReturn(99L);
    when(deviceFingerprintPort.fingerprint(TestValues.DEVICE_ID, TestValues.USER_AGENT))
        .thenReturn("fingerprint-hash");
    when(userDevicePort.findByUserEmailAndDeviceId(TestValues.USER_EMAIL, TestValues.DEVICE_ID))
        .thenReturn(Optional.of(userDevice));
    when(messageResolverPort.getMessage("security.jwt.session.stale"))
        .thenReturn("La sesion ya no es valida.");

    filter.doFilter(request, response, new MockFilterChain());

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getErrorMessage()).isEqualTo("La sesion ya no es valida.");
  }

  @Test
  @DisplayName(
      "Given an expired JWT when the filter is executed then it returns the localized expired token message")
  void givenAnExpiredJwtWhenTheFilterIsExecutedThenItReturnsTheLocalizedExpiredTokenMessage()
      throws Exception {
    final JwtTokenFilter filter =
        new JwtTokenFilter(
            jwtProperties,
            jwtProvider,
            messageResolverPort,
            deviceFingerprintPort,
            securityThreatProtectionPort,
            userDevicePort,
            securityRequestProperties);
    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(TestValues.AUTHORIZATION_HEADER, "Bearer access-token");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtProvider.parseClaims("access-token"))
        .thenThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "Token expired"));
    when(messageResolverPort.getMessage("jwt.token.error.expired.exception"))
        .thenReturn("El token expiro.");

    filter.doFilter(request, response, new MockFilterChain());

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getErrorMessage()).isEqualTo("El token expiro.");
  }
}
