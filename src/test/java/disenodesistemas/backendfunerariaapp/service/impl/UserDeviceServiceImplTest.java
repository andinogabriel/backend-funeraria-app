package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.utils.UserTestDataFactory.getUserEntity;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.DeviceInfo;
import disenodesistemas.backendfunerariaapp.entities.RefreshToken;
import disenodesistemas.backendfunerariaapp.entities.UserDevice;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.UserDeviceRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class UserDeviceServiceImplTest {

  @Mock private UserDeviceRepository userDeviceRepository;
  @InjectMocks private UserDeviceServiceImpl sut;
  private static UserDevice userDevice;

  @BeforeEach
  void setUp() {
    userDevice =
        new UserDevice(
            1L,
            getUserEntity(),
            "windows-10-desktop-Chrome-v117.0.0.0",
            "be7125c7-c1a9-44b1-b1e8-f46e27b3b07e",
            new RefreshToken(
                1L,
                "591ba392-49d4-45c4-be05-f44a49a2a2e0",
                userDevice,
                1L,
                Instant.parse("2024-12-13T10:15:30.345Z")),
            Boolean.FALSE);
  }

  @Test
  void findByUserId() {
    final Long userDeviceId = 1L;
    final UserDevice userDevice = new UserDevice();
    userDevice.setId(userDeviceId);
    given(userDeviceRepository.findByUserId(userDeviceId)).willReturn(Optional.of(userDevice));

    final UserDevice actualResult = sut.findByUserId(userDeviceId);

    assertAll(
        () -> assertNotNull(actualResult), () -> assertEquals(userDeviceId, actualResult.getId()));
    then(userDeviceRepository).should(times(1)).findByUserId(userDeviceId);
  }

  @Test
  void findByUserIdThrowsNotFoundException() {
    final Long NON_EXISTING_USER_DEVICE_ID = 1111L;
    given(userDeviceRepository.findByUserId(NON_EXISTING_USER_DEVICE_ID))
        .willThrow(new NotFoundException("user.device.error.not.found"));

    final NotFoundException exception =
        assertThrows(NotFoundException.class, () -> sut.findByUserId(NON_EXISTING_USER_DEVICE_ID));

    assertAll(
        () -> assertEquals(HttpStatus.NOT_FOUND, exception.getStatus()),
        () -> assertEquals("user.device.error.not.found", exception.getMessage()));
    then(userDeviceRepository).should(times(1)).findByUserId(NON_EXISTING_USER_DEVICE_ID);
  }

  @Test
  void findByUser() {
    final UserEntity user = getUserEntity();
    given(userDeviceRepository.findByUser(user)).willReturn(Optional.of(userDevice));

    final UserDevice actualResult = sut.findByUser(user);

    assertAll(
        () -> assertEquals(user.getEmail(), actualResult.getUser().getEmail()),
        () -> assertNotNull(actualResult.getDeviceId()));
    then(userDeviceRepository).should(times(1)).findByUser(user);
  }

  @Test
  void findByUserThrowsNotFoundException() {
    final UserEntity user = getUserEntity();
    given(userDeviceRepository.findByUser(user))
        .willThrow(new NotFoundException("user.error.email.not.registered"));

    final NotFoundException exception =
        assertThrows(NotFoundException.class, () -> sut.findByUser(user));

    assertAll(
        () -> assertEquals(HttpStatus.NOT_FOUND, exception.getStatus()),
        () -> assertEquals("user.error.email.not.registered", exception.getMessage()));
    then(userDeviceRepository).should(times(1)).findByUser(user);
  }

  @Test
  void createUserDevice() {
    final DeviceInfo deviceInfo =
        DeviceInfo.builder()
            .deviceId("be7125c7-c1a9-44b1-b1e8-f46e27b3b07e")
            .deviceType("windows-10-desktop-Chrome-v117.0.0.0")
            .build();
    final UserDevice actualResult = sut.createUserDevice(deviceInfo);

    assertAll(
        () -> assertNotNull(actualResult),
        () -> assertEquals(deviceInfo.getDeviceId(), actualResult.getDeviceId()),
        () -> assertEquals(deviceInfo.getDeviceType(), actualResult.getDeviceType()),
        () -> assertTrue(actualResult.getIsRefreshActive()));
  }

  @Test
  void verifyRefreshAvailability() {
    RefreshToken activeRefreshToken = new RefreshToken();
    UserDevice userDevice = new UserDevice();
    userDevice.setIsRefreshActive(true);
    given(userDeviceRepository.findByRefreshToken(activeRefreshToken))
        .willReturn(Optional.of(userDevice));
    assertDoesNotThrow(() -> sut.verifyRefreshAvailability(activeRefreshToken));
  }

  @Test
  void verifyRefreshAvailability_RefreshTokenInactive_ShouldThrowException() {
    final RefreshToken inactiveRefreshToken = new RefreshToken();
    final UserDevice userDevice = new UserDevice();
    userDevice.setIsRefreshActive(false);
    given(userDeviceRepository.findByRefreshToken(inactiveRefreshToken))
        .willReturn(Optional.of(userDevice));

    final AppException exception =
        assertThrows(AppException.class, () -> sut.verifyRefreshAvailability(inactiveRefreshToken));

    assertAll(
        () -> assertEquals(HttpStatus.EXPECTATION_FAILED, exception.getStatus()),
        () -> assertEquals("user.device.error.refresh.token.blocked", exception.getMessage()));
  }

  @Test
  void findByRefreshToken() {
    final RefreshToken refreshToken = new RefreshToken();
    refreshToken.setId(1L);
    refreshToken.setToken("591ba392-49d4-45c4-be05-f44a49a2a2e0");
    given(userDeviceRepository.findByRefreshToken(refreshToken))
        .willReturn(Optional.ofNullable(userDevice));

    final UserDevice actualResult = sut.findByRefreshToken(refreshToken);

    assertAll(
        () -> assertEquals(refreshToken.getId(), actualResult.getRefreshToken().getId()),
        () -> assertEquals(refreshToken.getToken(), actualResult.getRefreshToken().getToken()),
        () -> assertNotNull(actualResult.getDeviceId()));
  }

  @Test
  void findByRefreshToken_RefreshTokenNotFound_ShouldThrowException() {
    final RefreshToken nonExistentRefreshToken = new RefreshToken();
    given(userDeviceRepository.findByRefreshToken(nonExistentRefreshToken))
        .willReturn(Optional.empty());

    final AppException exception =
        assertThrows(AppException.class, () -> sut.findByRefreshToken(nonExistentRefreshToken));
    assertAll(
        () -> assertEquals(HttpStatus.EXPECTATION_FAILED, exception.getStatus()),
        () -> assertEquals("user.device.error.refresh.token.not.found", exception.getMessage()));
  }
}
