package disenodesistemas.backendfunerariaapp.application.usecase.user;

import disenodesistemas.backendfunerariaapp.application.model.RequestMetadata;
import disenodesistemas.backendfunerariaapp.application.port.out.DeviceFingerprintPort;
import disenodesistemas.backendfunerariaapp.application.port.out.UserDevicePort;
import disenodesistemas.backendfunerariaapp.domain.entity.RefreshToken;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.web.dto.DeviceInfo;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Coordinates the persisted device session attached to authentication flows such as login, logout
 * and refresh. It is responsible for normalizing device metadata, deriving the fingerprint used by
 * security checks and keeping the active session aligned with the requesting client device.
 */
@Service
@RequiredArgsConstructor
public class UserSessionDeviceService {

  private final UserDevicePort userDevicePort;
  private final DeviceFingerprintPort deviceFingerprintPort;
  private final UserSessionSecurityService userSessionSecurityService;

  /**
   * Loads the persisted device session associated with the supplied user aggregate. Callers use
   * this helper when a session must already exist, such as logout flows that operate on the active
   * device state previously established during login.
   */
  public UserDevice findByUser(final UserEntity userEntity) {
    return userDevicePort.findByUser(userEntity);
  }

  /**
   * Normalizes the device identifier extracted from the incoming request payload. Returning a
   * stable fallback value keeps session-binding logic deterministic even when a client omits the
   * device information structure entirely.
   */
  public String resolveDeviceId(final DeviceInfo deviceInfo) {
    return deviceInfo == null ? "unknown" : StringUtils.defaultIfBlank(deviceInfo.deviceId(), "unknown");
  }

  /**
   * Creates or refreshes the persisted device session during a successful login. The method
   * normalizes device metadata, recalculates the fingerprint and advances the token version so any
   * previously issued access token for that device becomes stale when necessary.
   */
  public UserDevice registerLoginDevice(
      final UserEntity userEntity,
      final DeviceInfo deviceInfo,
      final RequestMetadata requestMetadata) {
    final String deviceId = resolveDeviceId(deviceInfo);
    final String deviceType = resolveDeviceType(deviceInfo);
    final String fingerprintHash = resolveFingerprint(deviceId, requestMetadata);
    final UserDevice userDevice =
        userDevicePort
            .findOptionalByUser(userEntity)
            .orElseGet(() -> userDevicePort.createUserDevice(deviceInfo));

    userDevice.registerSession(
        userEntity,
        deviceId,
        deviceType,
        fingerprintHash,
        requestMetadata.ipAddress(),
        nextTokenVersion(userDevice),
        Instant.now());
    return userDevicePort.save(userDevice);
  }

  /**
   * Validates that the logout request comes from the same device that owns the active session. If
   * the request device id differs from the persisted session, the anomaly is reported as suspicious
   * activity and the logout is rejected before session state is modified.
   */
  public void assertLogoutAllowed(
      final String principal,
      final UserDevice userDevice,
      final DeviceInfo deviceInfo,
      final RequestMetadata requestMetadata) {
    final String requestDeviceId = resolveDeviceId(deviceInfo);
    if (!userDevice.hasDeviceId(requestDeviceId)) {
      userSessionSecurityService.recordSuspiciousRequest(
          principal, requestDeviceId, requestMetadata, "logout_device_id_mismatch");
      throw new AppException("user.error.invalid.device.id", HttpStatus.EXPECTATION_FAILED);
    }
  }

  /**
   * Refreshes the persisted device session after the refresh token has been resolved. It validates
   * the requesting device identity, confirms fingerprint continuity and advances the session state
   * so the next access token is tied to the latest known device metadata.
   */
  public UserDevice refreshSession(
      final RefreshToken refreshToken,
      final DeviceInfo deviceInfo,
      final RequestMetadata requestMetadata,
      final String principal) {
    final UserDevice userDevice = refreshToken.getUserDevice();
    final String requestDeviceId = resolveDeviceId(deviceInfo);

    userSessionSecurityService.assertRequestAllowed(principal, requestDeviceId, requestMetadata);
    verifyRefreshRequest(requestDeviceId, requestMetadata, userDevice, principal);
    userDevicePort.verifyRefreshAvailability(refreshToken);

    userDevice.activateRefresh(
        resolveFingerprint(requestDeviceId, requestMetadata),
        requestMetadata.ipAddress(),
        nextTokenVersion(userDevice),
        Instant.now());
    return userDevicePort.save(userDevice);
  }

  /**
   * Marks the current device session as inactive and increments its token version. This invalidates
   * already issued access tokens for that device because subsequent validations will detect a stale
   * session version or an inactive refresh state.
   */
  public UserDevice deactivateSession(final UserDevice userDevice) {
    userDevice.deactivateRefresh(nextTokenVersion(userDevice), Instant.now());
    return userDevicePort.save(userDevice);
  }

  /**
   * Validates that a refresh attempt is being made by the same device identity that owns the
   * persisted session. When either the device id or the derived fingerprint differs, the method
   * records a suspicious signal and aborts the refresh operation immediately.
   */
  private void verifyRefreshRequest(
      final String requestDeviceId,
      final RequestMetadata requestMetadata,
      final UserDevice userDevice,
      final String principal) {
    if (!Strings.CS.equals(requestDeviceId, userDevice.getDeviceId())) {
      userSessionSecurityService.recordSuspiciousRequest(
          principal, requestDeviceId, requestMetadata, "refresh_device_id_mismatch");
      throw new AppException("security.jwt.device.mismatch", HttpStatus.UNAUTHORIZED);
    }

    final String requestFingerprint = resolveFingerprint(requestDeviceId, requestMetadata);
    if (!userDevice.hasFingerprint(requestFingerprint)) {
      userSessionSecurityService.recordSuspiciousRequest(
          principal, requestDeviceId, requestMetadata, "refresh_device_fingerprint_mismatch");
      throw new AppException("security.jwt.device.mismatch", HttpStatus.UNAUTHORIZED);
    }
  }

  /**
   * Normalizes the device type value that is stored together with the session. This ensures that
   * device metadata remains consistent in persistence even when the client omits the field or
   * sends blank values.
   */
  private String resolveDeviceType(final DeviceInfo deviceInfo) {
    return deviceInfo == null ? "unknown" : StringUtils.defaultIfBlank(deviceInfo.deviceType(), "unknown");
  }

  /**
   * Recomputes the fingerprint used to bind the session to a concrete device and user-agent pair.
   * The resulting value is stored in the device session and later compared against JWT claims on
   * every authenticated request.
   */
  private String resolveFingerprint(final String deviceId, final RequestMetadata requestMetadata) {
    return deviceFingerprintPort.fingerprint(deviceId, requestMetadata.userAgent());
  }

  /**
   * Calculates the next token version for the device session lifecycle. Incrementing this value is
   * the mechanism used by the security layer to invalidate older access tokens without storing
   * every issued JWT server-side.
   */
  private Long nextTokenVersion(final UserDevice userDevice) {
    return userDevice.getTokenVersion() == null ? 1L : userDevice.getTokenVersion() + 1;
  }
}
