package disenodesistemas.backendfunerariaapp.application.usecase.user;

import disenodesistemas.backendfunerariaapp.application.model.RequestMetadata;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthIdempotencyPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.MessageResolverPort;
import disenodesistemas.backendfunerariaapp.application.port.out.RequestMetadataPort;
import disenodesistemas.backendfunerariaapp.domain.entity.RefreshToken;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.event.OnUserLogoutSuccessEvent;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.web.dto.DeviceInfo;
import disenodesistemas.backendfunerariaapp.web.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.LogOutRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.TokenRefreshRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.UserLoginDto;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates the public session lifecycle exposed by the API: login, refresh and logout. The
 * use case coordinates credential validation, device binding, idempotent retries, token rotation
 * and logout side effects so each request produces a consistent and auditable security outcome.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserSessionUseCase {

  private static final String LOGIN_OPERATION = "user.login";
  private static final String REFRESH_OPERATION = "user.refresh";
  private static final HexFormat HEX_FORMAT = HexFormat.of();

  private final UserAuthenticationService userAuthenticationService;
  private final UserSessionSecurityService userSessionSecurityService;
  private final UserSessionDeviceService userSessionDeviceService;
  private final UserSessionTokenService userSessionTokenService;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final AuthenticatedUserPort authenticatedUserPort;
  private final MessageResolverPort messageResolverPort;
  private final RequestMetadataPort requestMetadataPort;
  private final AuthIdempotencyPort authIdempotencyPort;

  /**
   * Executes the login flow from credential validation through token issuance. The method applies
   * threat checks, supports idempotent retries, persists device-bound session state and finally
   * returns the access and refresh tokens associated with the authenticated device.
   */
  @Transactional
  public JwtDto login(final UserLoginDto loginUser) {
    final RequestMetadata requestMetadata = requestMetadataPort.currentRequest();
    final String deviceId = userSessionDeviceService.resolveDeviceId(loginUser.deviceInfo());
    logLoginStarted(loginUser.email(), deviceId, requestMetadata);

    userSessionSecurityService.assertLoginAllowed(loginUser.email(), deviceId, requestMetadata);

    final String idempotencyFingerprint = loginFingerprint(loginUser, requestMetadata);
    final JwtDto cachedResponse =
        authIdempotencyPort
            .findJwtResponse(LOGIN_OPERATION, requestMetadata.idempotencyKey(), idempotencyFingerprint)
            .orElse(null);
    if (cachedResponse != null) {
      return cachedResponse;
    }

    final UserEntity user;
    try {
      user = userAuthenticationService.authenticate(loginUser);
    } catch (AppException ex) {
      logLoginRejected(loginUser.email(), deviceId, ex.getMessage());
      userSessionSecurityService.recordLoginFailure(loginUser.email(), deviceId, requestMetadata);
      throw ex;
    }

    userSessionSecurityService.recordLoginSuccess(loginUser.email(), deviceId, requestMetadata);

    final UserDevice userDevice =
        userSessionDeviceService.registerLoginDevice(user, loginUser.deviceInfo(), requestMetadata);
    final JwtDto jwtDto = userSessionTokenService.issueLoginTokens(user, userDevice);

    authIdempotencyPort.storeJwtResponse(
        LOGIN_OPERATION, requestMetadata.idempotencyKey(), idempotencyFingerprint, jwtDto);
    logLoginCompleted(user.getEmail(), userDevice);
    return jwtDto;
  }

  /**
   * Closes the current authenticated device session and publishes the logout side effects required
   * by the security layer. Besides deactivating refresh state, it emits the logout event used to
   * reject the just-invalidated access token before its natural expiration time.
   */
  @Transactional
  public OperationStatusModel logoutUser(final LogOutRequestDto logOutRequest) {
    final RequestMetadata requestMetadata = requestMetadataPort.currentRequest();
    final UserEntity userEntity = authenticatedUserPort.getAuthenticatedUser();
    final UserDevice userDevice = userSessionDeviceService.findByUser(userEntity);
    logLogoutStarted(userEntity.getEmail(), logOutRequest.deviceInfo());

    userSessionDeviceService.assertLogoutAllowed(
        userEntity.getEmail(), userDevice, logOutRequest.deviceInfo(), requestMetadata);
    final UserDevice updatedUserDevice = userSessionDeviceService.deactivateSession(userDevice);
    userSessionTokenService.revokeIfPresent(updatedUserDevice);

    final OnUserLogoutSuccessEvent logoutSuccessEvent =
        new OnUserLogoutSuccessEvent(userEntity.getEmail(), logOutRequest.token(), logOutRequest);
    applicationEventPublisher.publishEvent(logoutSuccessEvent);
    logLogoutCompleted(userEntity.getEmail(), updatedUserDevice);

    return OperationStatusModel.builder()
        .result(messageResolverPort.getMessage("user.logout.success"))
        .name("SUCCESS")
        .build();
  }

  /**
   * Performs the refresh flow for an already authenticated device session. It validates the raw
   * refresh token, verifies device continuity, supports idempotent retries and returns the rotated
   * token pair that replaces the previous access and refresh credentials.
   */
  @Transactional
  public JwtDto refreshJwtToken(final TokenRefreshRequestDto tokenRefreshRequestDto) {
    final RequestMetadata requestMetadata = requestMetadataPort.currentRequest();
    final String deviceId = userSessionDeviceService.resolveDeviceId(tokenRefreshRequestDto.deviceInfo());
    logRefreshStarted(deviceId, requestMetadata);

    final String idempotencyFingerprint =
        refreshFingerprint(tokenRefreshRequestDto, requestMetadata);
    final JwtDto cachedResponse =
        authIdempotencyPort
            .findJwtResponse(
                REFRESH_OPERATION, requestMetadata.idempotencyKey(), idempotencyFingerprint)
            .orElse(null);
    if (cachedResponse != null) {
      return cachedResponse;
    }

    final RefreshToken refreshToken =
        userSessionTokenService.resolveActiveRefreshToken(tokenRefreshRequestDto.refreshToken());
    final UserDevice userDevice =
        userSessionDeviceService.refreshSession(
            refreshToken, tokenRefreshRequestDto.deviceInfo(), requestMetadata, refreshToken.getUserDevice().getUser().getEmail());
    final UserEntity userEntity = userDevice.getUser();

    final JwtDto jwtDto = userSessionTokenService.rotateTokens(userEntity, userDevice, refreshToken);

    authIdempotencyPort.storeJwtResponse(
        REFRESH_OPERATION, requestMetadata.idempotencyKey(), idempotencyFingerprint, jwtDto);
    logRefreshCompleted(userEntity.getEmail(), userDevice);
    return jwtDto;
  }

  /**
   * Builds the idempotency fingerprint that uniquely represents a login request attempt. Sensitive
   * request attributes are combined and then hashed so retries can be safely matched without
   * storing raw credentials or device metadata in the idempotency cache.
   */
  private String loginFingerprint(
      final UserLoginDto loginUser, final RequestMetadata requestMetadata) {
    return sha256(
        LOGIN_OPERATION
            + '|'
            + loginUser.email()
            + '|'
            + loginUser.password()
            + '|'
            + userSessionDeviceService.resolveDeviceId(loginUser.deviceInfo())
            + '|'
            + requestMetadata.userAgent());
  }

  /**
   * Builds the idempotency fingerprint used to correlate refresh retries with the first successful
   * execution. The fingerprint combines the raw refresh token, normalized device identity and user
   * agent before hashing so replay detection remains deterministic and opaque.
   */
  private String refreshFingerprint(
      final TokenRefreshRequestDto tokenRefreshRequestDto, final RequestMetadata requestMetadata) {
    return sha256(
        REFRESH_OPERATION
            + '|'
            + tokenRefreshRequestDto.refreshToken()
            + '|'
            + userSessionDeviceService.resolveDeviceId(tokenRefreshRequestDto.deviceInfo())
            + '|'
            + requestMetadata.userAgent());
  }

  /**
   * Applies SHA-256 to the supplied idempotency material before it is stored or compared. This
   * keeps the idempotency layer deterministic while ensuring raw security-sensitive input is not
   * retained in plain text inside application memory or logs.
   */
  private String sha256(final String value) {
    try {
      return HEX_FORMAT.formatHex(
          MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 algorithm not available", ex);
    }
  }

  /**
   * Emits the structured log event that marks the beginning of the login flow. The log captures the
   * principal, device and request origin so troubleshooting can correlate authentication attempts
   * with later success, failure or blacklist decisions.
   */
  private void logLoginStarted(
      final String email, final String deviceId, final RequestMetadata requestMetadata) {
    log.atInfo()
        .addKeyValue("event", "user.login.started")
        .addKeyValue("email", email)
        .addKeyValue("deviceId", deviceId)
        .addKeyValue("ipAddress", requestMetadata.ipAddress())
        .log("user.login.started");
  }

  /**
   * Emits the structured rejection log for failed login attempts. The event records the principal,
   * device identifier and rejection reason so operators can diagnose whether the failure came from
   * bad credentials, throttling, blocking or another security rule.
   */
  private void logLoginRejected(final String email, final String deviceId, final String reason) {
    log.atWarn()
        .addKeyValue("event", "user.login.rejected")
        .addKeyValue("email", email)
        .addKeyValue("deviceId", deviceId)
        .addKeyValue("reason", reason)
        .log("user.login.rejected");
  }

  /**
   * Emits the completion log once the device session and token state were fully persisted. The
   * token version is included so later security events can be correlated with the exact session
   * generation that produced the issued credentials.
   */
  private void logLoginCompleted(final String email, final UserDevice userDevice) {
    log.atInfo()
        .addKeyValue("event", "user.login.completed")
        .addKeyValue("email", email)
        .addKeyValue("deviceId", userDevice.getDeviceId())
        .addKeyValue("tokenVersion", userDevice.getTokenVersion())
        .log("user.login.completed");
  }

  /**
   * Emits the structured log that marks the beginning of logout processing. Logging the request
   * email and requested device id helps distinguish legitimate session closures from device-mismatch
   * attempts that may later be reported as suspicious activity.
   */
  private void logLogoutStarted(final String email, final DeviceInfo deviceInfo) {
    log.atInfo()
        .addKeyValue("event", "user.logout.started")
        .addKeyValue("email", email)
        .addKeyValue(
            "deviceId",
            deviceInfo == null ? null : userSessionDeviceService.resolveDeviceId(deviceInfo))
        .log("user.logout.started");
  }

  /**
   * Emits the final logout log after the device session was deactivated successfully. The event
   * includes the effective device id and resulting token version so stale-token investigations can
   * be correlated with the logout that invalidated the session.
   */
  private void logLogoutCompleted(final String email, final UserDevice userDevice) {
    log.atInfo()
        .addKeyValue("event", "user.logout.completed")
        .addKeyValue("email", email)
        .addKeyValue("deviceId", userDevice.getDeviceId())
        .addKeyValue("tokenVersion", userDevice.getTokenVersion())
        .log("user.logout.completed");
  }

  /**
   * Emits the structured log that marks the start of refresh-token processing. The event focuses on
   * the requesting device and source IP because those values are the most relevant when diagnosing
   * suspicious refresh attempts or idempotent replay behavior.
   */
  private void logRefreshStarted(final String deviceId, final RequestMetadata requestMetadata) {
    log.atInfo()
        .addKeyValue("event", "user.token.refresh.started")
        .addKeyValue("deviceId", deviceId)
        .addKeyValue("ipAddress", requestMetadata.ipAddress())
        .log("user.token.refresh.started");
  }

  /**
   * Emits the completion log once the refresh flow persisted the rotated device session and token
   * state. Including the token version makes it easier to correlate later JWT validations with the
   * exact refresh operation that generated the latest access token.
   */
  private void logRefreshCompleted(final String email, final UserDevice userDevice) {
    log.atInfo()
        .addKeyValue("event", "user.token.refresh.completed")
        .addKeyValue("email", email)
        .addKeyValue("deviceId", userDevice.getDeviceId())
        .addKeyValue("tokenVersion", userDevice.getTokenVersion())
        .log("user.token.refresh.completed");
  }
}
