package disenodesistemas.backendfunerariaapp.modern.application.usecase.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.model.RequestMetadata;
import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthIdempotencyPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.MessageResolverPort;
import disenodesistemas.backendfunerariaapp.application.port.out.RequestMetadataPort;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserAuthenticationService;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionDeviceService;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionSecurityService;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionTokenService;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.RefreshToken;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.event.OnUserLogoutSuccessEvent;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.exception.RefreshTokenReuseException;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.web.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.LogOutRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.TokenRefreshRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.UserLoginDto;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSessionUseCase")
class UserSessionUseCaseTest {

  @Mock private UserAuthenticationService userAuthenticationService;
  @Mock private UserSessionSecurityService userSessionSecurityService;
  @Mock private UserSessionDeviceService userSessionDeviceService;
  @Mock private UserSessionTokenService userSessionTokenService;
  @Mock private ApplicationEventPublisher applicationEventPublisher;
  @Mock private AuthenticatedUserPort authenticatedUserPort;
  @Mock private MessageResolverPort messageResolverPort;
  @Mock private RequestMetadataPort requestMetadataPort;
  @Mock private AuthIdempotencyPort authIdempotencyPort;
  @Mock private AuditEventPort auditEventPort;

  @InjectMocks private UserSessionUseCase userSessionUseCase;

  @Test
  @DisplayName(
      "Given valid credentials and an active device when login is requested then it returns fresh access and refresh tokens")
  void givenValidCredentialsAndAnActiveDeviceWhenLoginIsRequestedThenItReturnsFreshAccessAndRefreshTokens() {
    final UserEntity userEntity = SecurityTestDataFactory.userEntity();
    final RequestMetadata requestMetadata = SecurityTestDataFactory.requestMetadata();
    final UserLoginDto loginDto =
        UserLoginDto.builder()
            .email(userEntity.getEmail())
            .password("password-123")
            .deviceInfo(SecurityTestDataFactory.deviceInfo())
            .build();
    final UserDevice persistedDevice = SecurityTestDataFactory.userDevice(userEntity);
    final JwtDto issuedTokens =
        JwtDto.builder()
            .authorization("Bearer access-token")
            .refreshToken("refresh-token")
            .expiryDuration(900_000L)
            .authorities(java.util.List.of("ROLE_USER"))
            .build();

    when(requestMetadataPort.currentRequest()).thenReturn(requestMetadata);
    when(userSessionDeviceService.resolveDeviceId(loginDto.deviceInfo()))
        .thenReturn(loginDto.deviceInfo().deviceId());
    when(authIdempotencyPort.findJwtResponse(any(), any(), any())).thenReturn(Optional.empty());
    when(userAuthenticationService.authenticate(loginDto)).thenReturn(userEntity);
    when(userSessionDeviceService.registerLoginDevice(userEntity, loginDto.deviceInfo(), requestMetadata))
        .thenReturn(persistedDevice);
    when(userSessionTokenService.issueLoginTokens(userEntity, persistedDevice)).thenReturn(issuedTokens);

    final JwtDto response = userSessionUseCase.login(loginDto);

    assertThat(response.authorization()).isEqualTo("Bearer access-token");
    assertThat(response.refreshToken()).isEqualTo("refresh-token");
    assertThat(response.authorities()).containsExactly("ROLE_USER");
    verify(userSessionSecurityService)
        .assertLoginAllowed(userEntity.getEmail(), loginDto.deviceInfo().deviceId(), requestMetadata);
    verify(userSessionSecurityService)
        .recordLoginSuccess(userEntity.getEmail(), loginDto.deviceInfo().deviceId(), requestMetadata);
    verify(authIdempotencyPort).storeJwtResponse(eq("user.login"), eq("idem-123"), any(), eq(response));
  }

  @Test
  @DisplayName(
      "Given a repeated login request with the same idempotency key when login is requested then it reuses the cached token response")
  void givenARepeatedLoginRequestWithTheSameIdempotencyKeyWhenLoginIsRequestedThenItReusesTheCachedTokenResponse() {
    final RequestMetadata requestMetadata = SecurityTestDataFactory.requestMetadata();
    final UserLoginDto loginDto =
        UserLoginDto.builder()
            .email("john.doe@example.com")
            .password("password-123")
            .deviceInfo(SecurityTestDataFactory.deviceInfo())
            .build();
    final JwtDto cachedResponse =
        JwtDto.builder()
            .authorization("Bearer cached-access")
            .refreshToken("cached-refresh")
            .expiryDuration(900_000L)
            .authorities(java.util.List.of("ROLE_USER"))
            .build();

    when(requestMetadataPort.currentRequest()).thenReturn(requestMetadata);
    when(userSessionDeviceService.resolveDeviceId(loginDto.deviceInfo()))
        .thenReturn(loginDto.deviceInfo().deviceId());
    when(authIdempotencyPort.findJwtResponse(any(), any(), any())).thenReturn(Optional.of(cachedResponse));

    final JwtDto response = userSessionUseCase.login(loginDto);

    assertThat(response).isEqualTo(cachedResponse);
    verify(userAuthenticationService, never()).authenticate(any());
    verify(authIdempotencyPort, never()).storeJwtResponse(any(), any(), any(), any());
  }

  @Test
  @DisplayName(
      "Given an invalid password when login is requested then it records the failed attempt and rejects the authentication")
  void givenAnInvalidPasswordWhenLoginIsRequestedThenItRecordsTheFailedAttemptAndRejectsTheAuthentication() {
    final UserEntity userEntity = SecurityTestDataFactory.userEntity();
    final RequestMetadata requestMetadata = SecurityTestDataFactory.requestMetadata();
    final UserLoginDto loginDto =
        UserLoginDto.builder()
            .email(userEntity.getEmail())
            .password("wrong-password")
            .deviceInfo(SecurityTestDataFactory.deviceInfo())
            .build();

    when(requestMetadataPort.currentRequest()).thenReturn(requestMetadata);
    when(userSessionDeviceService.resolveDeviceId(loginDto.deviceInfo()))
        .thenReturn(loginDto.deviceInfo().deviceId());
    when(authIdempotencyPort.findJwtResponse(any(), any(), any())).thenReturn(Optional.empty());
    when(userAuthenticationService.authenticate(loginDto))
        .thenThrow(new AppException("password.error.wrong", org.springframework.http.HttpStatus.UNAUTHORIZED));

    assertThatThrownBy(() -> userSessionUseCase.login(loginDto))
        .isInstanceOf(AppException.class)
        .extracting("message")
        .isEqualTo("password.error.wrong");

    verify(userSessionSecurityService)
        .recordLoginFailure(userEntity.getEmail(), loginDto.deviceInfo().deviceId(), requestMetadata);
  }

  @Test
  @DisplayName(
      "Given an active refresh token bound to the same device when refresh is requested then it rotates the refresh token and returns a new access token")
  void givenAnActiveRefreshTokenBoundToTheSameDeviceWhenRefreshIsRequestedThenItRotatesTheRefreshTokenAndReturnsANewAccessToken() {
    final UserEntity userEntity = SecurityTestDataFactory.userEntity();
    final RequestMetadata requestMetadata = SecurityTestDataFactory.requestMetadata();
    final UserDevice userDevice = SecurityTestDataFactory.userDevice(userEntity);
    final RefreshToken refreshToken = SecurityTestDataFactory.refreshToken(userDevice);
    final TokenRefreshRequestDto refreshRequest =
        TokenRefreshRequestDto.builder().deviceInfo(SecurityTestDataFactory.deviceInfo()).build();

    when(requestMetadataPort.currentRequest()).thenReturn(requestMetadata);
    when(userSessionDeviceService.resolveDeviceId(refreshRequest.deviceInfo()))
        .thenReturn(refreshRequest.deviceInfo().deviceId());
    when(authIdempotencyPort.findJwtResponse(any(), any(), any())).thenReturn(Optional.empty());
    when(userSessionTokenService.findCurrent("raw-refresh-token")).thenReturn(Optional.of(refreshToken));
    when(
            userSessionDeviceService.refreshSession(
                refreshToken, refreshRequest.deviceInfo(), requestMetadata, userEntity.getEmail()))
        .thenReturn(userDevice);
    when(userSessionTokenService.rotateTokens(userEntity, userDevice, refreshToken))
        .thenReturn(
            JwtDto.builder()
                .authorization("Bearer new-access-token")
                .refreshToken("rotated-refresh-token")
                .expiryDuration(900_000L)
                .authorities(java.util.List.of("ROLE_USER"))
                .build());

    final JwtDto response = userSessionUseCase.refreshJwtToken("raw-refresh-token", refreshRequest);

    assertThat(response.authorization()).isEqualTo("Bearer new-access-token");
    assertThat(response.refreshToken()).isEqualTo("rotated-refresh-token");
    verify(userSessionTokenService).verifyActive(refreshToken);
    verify(authIdempotencyPort).storeJwtResponse(eq("user.refresh"), eq("idem-123"), any(), eq(response));
  }

  @Test
  @DisplayName(
      "Given no refresh cookie when refresh is requested then it fails with 401 before touching any session state")
  void givenNoRefreshCookieWhenRefreshIsRequestedThenItFailsWith401BeforeTouchingAnySessionState() {
    final TokenRefreshRequestDto refreshRequest =
        TokenRefreshRequestDto.builder().deviceInfo(SecurityTestDataFactory.deviceInfo()).build();

    assertThatThrownBy(() -> userSessionUseCase.refreshJwtToken(" ", refreshRequest))
        .isInstanceOf(AppException.class)
        .hasMessage("refresh.token.error.blank.token")
        .extracting("status")
        .isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThatThrownBy(() -> userSessionUseCase.refreshJwtToken(null, refreshRequest))
        .hasMessage("refresh.token.error.blank.token");
    verifyNoInteractions(requestMetadataPort, userSessionTokenService, authIdempotencyPort);
  }

  @Test
  @DisplayName(
      "Given a token that is neither current nor superseded when refresh is requested then it fails with not found")
  void givenATokenThatIsNeitherCurrentNorSupersededWhenRefreshIsRequestedThenItFailsWithNotFound() {
    final TokenRefreshRequestDto refreshRequest = stubRefreshRequest();
    when(userSessionTokenService.findCurrent("unknown")).thenReturn(Optional.empty());
    when(userSessionTokenService.findSuperseded("unknown")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userSessionUseCase.refreshJwtToken("unknown", refreshRequest))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("refresh.token.error.not.found");
  }

  @Test
  @DisplayName(
      "Given a just-rotated token presented within the grace window when refresh is requested then it answers 409 and leaves the session alone")
  void givenAJustRotatedTokenPresentedWithinTheGraceWindowWhenRefreshIsRequestedThenItAnswers409AndLeavesTheSessionAlone() {
    final UserDevice userDevice = SecurityTestDataFactory.userDevice(SecurityTestDataFactory.userEntity());
    final RefreshToken refreshToken = SecurityTestDataFactory.refreshToken(userDevice);
    final TokenRefreshRequestDto refreshRequest = stubRefreshRequest();
    when(userSessionTokenService.findCurrent("previous")).thenReturn(Optional.empty());
    when(userSessionTokenService.findSuperseded("previous")).thenReturn(Optional.of(refreshToken));
    when(userSessionTokenService.isWithinReuseGrace(refreshToken)).thenReturn(true);

    assertThatThrownBy(() -> userSessionUseCase.refreshJwtToken("previous", refreshRequest))
        .isInstanceOf(ConflictException.class)
        .hasMessage("refresh.token.error.superseded");
    verify(userSessionTokenService).verifyActive(refreshToken);
    verify(userSessionDeviceService, never()).deactivateSession(any());
    verifyNoInteractions(auditEventPort);
  }

  @Test
  @DisplayName(
      "Given a rotated token replayed after the grace window when refresh is requested then it closes the device session, audits the reuse and fails with 401")
  void givenARotatedTokenReplayedAfterTheGraceWindowWhenRefreshIsRequestedThenItClosesTheDeviceSessionAuditsTheReuseAndFailsWith401() {
    final UserEntity userEntity = SecurityTestDataFactory.userEntity();
    final UserDevice userDevice = SecurityTestDataFactory.userDevice(userEntity);
    final RefreshToken refreshToken = SecurityTestDataFactory.refreshToken(userDevice);
    final TokenRefreshRequestDto refreshRequest = stubRefreshRequest();
    final RequestMetadata requestMetadata = SecurityTestDataFactory.requestMetadata();
    when(userSessionTokenService.findCurrent("stolen")).thenReturn(Optional.empty());
    when(userSessionTokenService.findSuperseded("stolen")).thenReturn(Optional.of(refreshToken));
    when(userSessionTokenService.isWithinReuseGrace(refreshToken)).thenReturn(false);
    when(userSessionDeviceService.deactivateSession(userDevice)).thenReturn(userDevice);

    assertThatThrownBy(() -> userSessionUseCase.refreshJwtToken("stolen", refreshRequest))
        .isInstanceOf(RefreshTokenReuseException.class)
        .hasMessage("refresh.token.error.reused")
        .extracting("status")
        .isEqualTo(HttpStatus.UNAUTHORIZED);

    final String deviceId = refreshRequest.deviceInfo().deviceId();
    // Not fed to the blacklist: that would let a thief lock the real user out.
    verifyNoInteractions(userSessionSecurityService);
    verify(userSessionDeviceService).deactivateSession(userDevice);
    verify(userSessionTokenService).revokeIfPresent(userDevice);
    final ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
    verify(auditEventPort)
        .record(
            eq(AuditAction.REFRESH_TOKEN_REUSE_DETECTED),
            eq(userEntity.getEmail()),
            eq(userEntity.getId()),
            eq("USER_DEVICE"),
            eq(userDevice.getDeviceId()),
            payload.capture());
    assertThat(payload.getValue())
        .isEqualTo(
            "{\"requestDeviceId\":\""
                + deviceId
                + "\",\"ipAddress\":\""
                + requestMetadata.ipAddress()
                + "\"}");
    verify(userSessionTokenService, never()).rotateTokens(any(), any(), any());
  }

  @Test
  @DisplayName(
      "Given the refresh use case when its transaction is inspected then a detected reuse commits the revocation instead of rolling it back")
  void givenTheRefreshUseCaseWhenItsTransactionIsInspectedThenADetectedReuseCommitsTheRevocationInsteadOfRollingItBack()
      throws NoSuchMethodException {
    final Transactional transactional =
        UserSessionUseCase.class
            .getMethod("refreshJwtToken", String.class, TokenRefreshRequestDto.class)
            .getAnnotation(Transactional.class);

    assertThat(transactional.noRollbackFor()).containsExactly(RefreshTokenReuseException.class);
  }

  private TokenRefreshRequestDto stubRefreshRequest() {
    final TokenRefreshRequestDto refreshRequest =
        TokenRefreshRequestDto.builder().deviceInfo(SecurityTestDataFactory.deviceInfo()).build();
    when(requestMetadataPort.currentRequest()).thenReturn(SecurityTestDataFactory.requestMetadata());
    when(userSessionDeviceService.resolveDeviceId(refreshRequest.deviceInfo()))
        .thenReturn(refreshRequest.deviceInfo().deviceId());
    when(authIdempotencyPort.findJwtResponse(any(), any(), any())).thenReturn(Optional.empty());
    return refreshRequest;
  }

  @Test
  @DisplayName(
      "Given the authenticated user and the matching device when logout is requested then it revokes the refresh token and publishes the logout event")
  void givenTheAuthenticatedUserAndTheMatchingDeviceWhenLogoutIsRequestedThenItRevokesTheRefreshTokenAndPublishesTheLogoutEvent() {
    final UserEntity userEntity = SecurityTestDataFactory.userEntity();
    final UserDevice userDevice = SecurityTestDataFactory.userDevice(userEntity);
    final RefreshToken refreshToken = SecurityTestDataFactory.refreshToken(userDevice);
    final LogOutRequestDto logoutRequest =
        LogOutRequestDto.builder()
            .token("access-token")
            .deviceInfo(SecurityTestDataFactory.deviceInfo())
            .build();

    when(requestMetadataPort.currentRequest()).thenReturn(SecurityTestDataFactory.requestMetadata());
    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(userEntity);
    when(userSessionDeviceService.findByUser(userEntity)).thenReturn(userDevice);
    when(userSessionDeviceService.deactivateSession(userDevice)).thenReturn(userDevice);
    when(messageResolverPort.getMessage("user.logout.success")).thenReturn("Sesion cerrada correctamente");

    final OperationStatusModel response = userSessionUseCase.logoutUser(logoutRequest);

    assertThat(response.result()).isEqualTo("Sesion cerrada correctamente");
    verify(userSessionTokenService).revokeIfPresent(userDevice);
    final ArgumentCaptor<OnUserLogoutSuccessEvent> eventCaptor =
        ArgumentCaptor.forClass(OnUserLogoutSuccessEvent.class);
    verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
    assertThat(eventCaptor.getValue().userEmail()).isEqualTo(userEntity.getEmail());
  }
}
