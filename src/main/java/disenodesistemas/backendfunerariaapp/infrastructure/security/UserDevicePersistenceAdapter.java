package disenodesistemas.backendfunerariaapp.infrastructure.security;

import static org.springframework.http.HttpStatus.EXPECTATION_FAILED;

import disenodesistemas.backendfunerariaapp.application.port.out.UserDevicePort;
import disenodesistemas.backendfunerariaapp.domain.entity.RefreshToken;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.UserDeviceRepository;
import disenodesistemas.backendfunerariaapp.web.dto.DeviceInfo;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides the persistence adapter for the device-scoped session aggregate used by authentication
 * flows. It loads and stores the session state that ties JWT and refresh-token activity to a
 * concrete device, including refresh availability and token-version continuity.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDevicePersistenceAdapter implements UserDevicePort {

  private final UserDeviceRepository userDeviceRepository;

  /**
   * Looks up the session a user holds on one device. A user has one session per device id, so
   * login reuses the row of a device that signed in before and creates one for a new device.
   */
  @Override
  public Optional<UserDevice> findByUserAndDeviceId(
      final UserEntity userEntity, final String deviceId) {
    return userDeviceRepository.findByUserAndDeviceId(userEntity, deviceId);
  }

  /** Sessions still flagged as able to refresh, most recently used first. */
  @Override
  public List<UserDevice> findActiveByUser(final UserEntity userEntity) {
    return userDeviceRepository.findByUserAndIsRefreshActiveTrueOrderByLastSeenAtDesc(userEntity);
  }

  /** A session by id, scoped to its owner so one user can never reach another user's session. */
  @Override
  public Optional<UserDevice> findByIdAndUser(final Long id, final UserEntity userEntity) {
    return userDeviceRepository.findByIdAndUser(id, userEntity);
  }

  /**
   * Looks up the persisted device session that should own a JWT carrying the given principal and
   * device id. JWT validation uses this query to reconnect the token claims with the server-side
   * session state that tracks fingerprint, refresh activity and token version.
   */
  @Override
  public Optional<UserDevice> findByUserEmailAndDeviceId(final String email, final String deviceId) {
    return userDeviceRepository.findByUserEmailAndDeviceId(email, deviceId);
  }

  /**
   * Creates the initial device-session aggregate used by the first successful login of a user. The
   * new entity is initialized with device identity information and a refresh-active state so later
   * login and refresh flows can persist the rest of the session lifecycle.
   */
  @Override
  public UserDevice createUserDevice(final DeviceInfo deviceInfo) {
    log.atInfo()
        .addKeyValue("event", "user.device.create")
        .addKeyValue("deviceId", deviceInfo.deviceId())
        .addKeyValue("deviceType", deviceInfo.deviceType())
        .log("user.device.create");
    final UserDevice userDevice = new UserDevice();
    userDevice.identifyDevice(deviceInfo.deviceId(), deviceInfo.deviceType());
    userDevice.setIsRefreshActive(Boolean.TRUE);
    return userDevice;
  }

  /**
   * Persists the latest device-session state after login, refresh or logout updates. Centralizing
   * the save operation behind the port keeps application use cases independent from repository
   * details while still allowing transactional writes in the adapter layer.
   */
  @Override
  @Transactional
  public UserDevice save(final UserDevice userDevice) {
    return userDeviceRepository.save(userDevice);
  }

  /**
   * Ensures that refresh operations are still allowed for the device session that owns the token.
   * This protects the refresh flow from reactivating sessions that were explicitly deactivated by
   * logout or another security event.
   */
  @Override
  public void verifyRefreshAvailability(final RefreshToken refreshToken) {
    final UserDevice userDevice = findByRefreshToken(refreshToken);
    if (Boolean.FALSE.equals(userDevice.getIsRefreshActive())) {
      log.atWarn()
          .addKeyValue("event", "user.token.refresh.rejected")
          .addKeyValue("deviceId", userDevice.getDeviceId())
          .addKeyValue("reason", "refresh_blocked")
          .log("user.token.refresh.rejected");
      throw new AppException("user.device.error.refresh.token.blocked", EXPECTATION_FAILED);
    }
  }

  /**
   * Returns the device session that owns the supplied refresh-token aggregate. This association is
   * required when the refresh flow needs to validate session activity or mutate device-bound state
   * before issuing a new access token.
   */
  @Override
  public UserDevice findByRefreshToken(final RefreshToken refreshToken) {
    return userDeviceRepository
        .findByRefreshToken(refreshToken)
        .orElseThrow(
            () -> new AppException("user.device.error.refresh.token.not.found", EXPECTATION_FAILED));
  }
}
