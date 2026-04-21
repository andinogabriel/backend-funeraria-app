package disenodesistemas.backendfunerariaapp.modern.application.usecase.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    when(userDevicePort.findOptionalByUser(userEntity)).thenReturn(Optional.empty());
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
}
