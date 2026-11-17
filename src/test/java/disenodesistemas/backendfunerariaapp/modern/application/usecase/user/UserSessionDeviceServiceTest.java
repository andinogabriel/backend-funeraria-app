package disenodesistemas.backendfunerariaapp.modern.application.usecase.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.model.RequestMetadata;
import disenodesistemas.backendfunerariaapp.application.port.out.DeviceFingerprintPort;
import disenodesistemas.backendfunerariaapp.application.port.out.UserDevicePort;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionDeviceService;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionSecurityService;
import disenodesistemas.backendfunerariaapp.domain.entity.RefreshToken;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.web.dto.DeviceInfo;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSessionDeviceService")
class UserSessionDeviceServiceTest {

  @Mock private UserDevicePort userDevicePort;
  @Mock private DeviceFingerprintPort deviceFingerprintPort;
  @Mock private UserSessionSecurityService userSessionSecurityService;

  @InjectMocks private UserSessionDeviceService userSessionDeviceService;

  @Test
  @DisplayName(
      "Given a login request for a user without a stored device when the device session is registered then it creates the device and persists the activated session")
  void givenALoginRequestForAUserWithoutAStoredDeviceWhenTheDeviceSessionIsRegisteredThenItCreatesTheDeviceAndPersistsTheActivatedSession() {
    final UserEntity userEntity = SecurityTestDataFactory.userEntity();
    final RequestMetadata requestMetadata = SecurityTestDataFactory.requestMetadata();
    final UserDevice newDevice = new UserDevice();

    when(userDevicePort.findByUserAndDeviceId(userEntity, "device-123")).thenReturn(Optional.empty());
    when(userDevicePort.createUserDevice(SecurityTestDataFactory.deviceInfo())).thenReturn(newDevice);
    when(deviceFingerprintPort.fingerprint("device-123", requestMetadata.userAgent()))
        .thenReturn("fingerprint-hash");
    when(userDevicePort.save(newDevice)).thenReturn(newDevice);

    final UserDevice persisted =
        userSessionDeviceService.registerLoginDevice(
            userEntity, SecurityTestDataFactory.deviceInfo(), requestMetadata);

    assertThat(persisted.getUser()).isEqualTo(userEntity);
    assertThat(persisted.getDeviceId()).isEqualTo("device-123");
    assertThat(persisted.getIsRefreshActive()).isTrue();
    assertThat(persisted.getTokenVersion()).isEqualTo(1L);
  }

  @Test
  @DisplayName(
      "Given a refresh request with a mismatched device id when the device session is refreshed then it records the suspicious request and rejects the refresh")
  void givenARefreshRequestWithAMismatchedDeviceIdWhenTheDeviceSessionIsRefreshedThenItRecordsTheSuspiciousRequestAndRejectsTheRefresh() {
    final UserEntity userEntity = SecurityTestDataFactory.userEntity();
    final RequestMetadata requestMetadata = SecurityTestDataFactory.requestMetadata();
    final UserDevice userDevice = SecurityTestDataFactory.userDevice(userEntity);
    final RefreshToken refreshToken = SecurityTestDataFactory.refreshToken(userDevice);

    assertThatThrownBy(
            () ->
                userSessionDeviceService.refreshSession(
                    refreshToken,
                    new disenodesistemas.backendfunerariaapp.web.dto.DeviceInfo("device-999", "mobile"),
                    requestMetadata,
                    userEntity.getEmail()))
        .isInstanceOf(AppException.class)
        .extracting("message")
        .isEqualTo("security.jwt.device.mismatch");

    verify(userSessionSecurityService)
        .assertRequestAllowed(userEntity.getEmail(), "device-999", requestMetadata);
    verify(userSessionSecurityService)
        .recordSuspiciousRequest(
            userEntity.getEmail(), "device-999", requestMetadata, "refresh_device_id_mismatch");
  }

  @Test
  @DisplayName(
      "Given an active device session when the device session is deactivated then it disables refresh and increments the token version before persisting")
  void givenAnActiveDeviceSessionWhenTheDeviceSessionIsDeactivatedThenItDisablesRefreshAndIncrementsTheTokenVersionBeforePersisting() {
    final UserDevice userDevice = SecurityTestDataFactory.userDevice(SecurityTestDataFactory.userEntity());

    when(userDevicePort.save(userDevice)).thenReturn(userDevice);

    final UserDevice updated = userSessionDeviceService.deactivateSession(userDevice);

    assertThat(updated.getIsRefreshActive()).isFalse();
    assertThat(updated.getTokenVersion()).isEqualTo(3L);
    verify(userDevicePort).save(userDevice);
  }

  @Test
  @DisplayName(
      "Given a user with a session on another device when a new device logs in then the existing session is left untouched")
  void givenASessionOnAnotherDeviceWhenANewDeviceLogsInThenTheExistingSessionIsLeftUntouched() {
    final UserEntity userEntity = SecurityTestDataFactory.userEntity();
    final RequestMetadata requestMetadata = SecurityTestDataFactory.requestMetadata();
    final UserDevice newDevice = new UserDevice();

    when(userDevicePort.findByUserAndDeviceId(userEntity, "device-123")).thenReturn(Optional.empty());
    when(userDevicePort.createUserDevice(SecurityTestDataFactory.deviceInfo())).thenReturn(newDevice);
    when(deviceFingerprintPort.fingerprint("device-123", requestMetadata.userAgent()))
        .thenReturn("fingerprint-hash");
    when(userDevicePort.save(newDevice)).thenReturn(newDevice);

    final UserDevice persisted =
        userSessionDeviceService.registerLoginDevice(
            userEntity, SecurityTestDataFactory.deviceInfo(), requestMetadata);

    assertThat(persisted.getUserAgent()).isEqualTo(requestMetadata.userAgent());
    assertThat(persisted.getCreatedAt()).isNotNull();
    verify(userDevicePort, never()).findActiveByUser(userEntity);
  }

  @Test
  @DisplayName(
      "Given a logout from a device with no session for the user when the logout session is resolved then it records the suspicious request and rejects it")
  void givenALogoutFromAnUnknownDeviceWhenTheSessionIsResolvedThenItIsRejected() {
    final UserEntity userEntity = SecurityTestDataFactory.userEntity();
    final RequestMetadata requestMetadata = SecurityTestDataFactory.requestMetadata();
    final DeviceInfo unknown = new DeviceInfo("device-999", "BROWSER");
    when(userDevicePort.findByUserAndDeviceId(userEntity, "device-999")).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> userSessionDeviceService.findLogoutSession(userEntity, unknown, requestMetadata))
        .isInstanceOf(AppException.class)
        .hasMessage("user.error.invalid.device.id");
    verify(userSessionSecurityService)
        .recordSuspiciousRequest(
            userEntity.getEmail(), "device-999", requestMetadata, "logout_device_id_mismatch");
  }

  @Test
  @DisplayName("Given a session id the user does not own when it is looked up then it is not found")
  void givenASessionIdTheUserDoesNotOwnWhenItIsLookedUpThenItIsNotFound() {
    final UserEntity userEntity = SecurityTestDataFactory.userEntity();
    when(userDevicePort.findByIdAndUser(99L, userEntity)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userSessionDeviceService.findOwnedSession(99L, userEntity))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("user.session.error.not.found");
  }
}
