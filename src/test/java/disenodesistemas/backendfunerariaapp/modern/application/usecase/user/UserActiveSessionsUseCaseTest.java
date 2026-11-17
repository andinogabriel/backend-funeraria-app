package disenodesistemas.backendfunerariaapp.modern.application.usecase.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.RequestMetadataPort;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserActiveSessionsUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionDeviceService;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionRevocationService;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import disenodesistemas.backendfunerariaapp.modern.support.TestValues;
import disenodesistemas.backendfunerariaapp.web.dto.response.UserSessionResponseDto;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("UserActiveSessionsUseCase")
class UserActiveSessionsUseCaseTest {

  @Mock private AuthenticatedUserPort authenticatedUserPort;
  @Mock private RequestMetadataPort requestMetadataPort;
  @Mock private UserSessionDeviceService userSessionDeviceService;
  @Mock private UserSessionRevocationService userSessionRevocationService;

  @InjectMocks private UserActiveSessionsUseCase useCase;

  private UserEntity user;

  @BeforeEach
  void setUp() {
    user = SecurityTestDataFactory.userEntity();
    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(user);
    lenient().when(requestMetadataPort.currentRequest()).thenReturn(SecurityTestDataFactory.requestMetadata());
  }

  @Test
  @DisplayName(
      "Given sessions on two devices and an expired one when they are listed then only refreshable sessions come back with the current one flagged")
  void listsRefreshableSessionsAndFlagsTheCurrentOne() {
    final UserDevice current = session(10L, TestValues.DEVICE_ID);
    final UserDevice other = session(11L, TestValues.ALTERNATE_DEVICE_ID);
    final UserDevice expired = session(12L, "device-expired");
    expired.getRefreshToken().setExpiryDate(Instant.now().minusSeconds(1));
    final UserDevice revoked = session(13L, "device-revoked");
    revoked.getRefreshToken().setRevokedAt(Instant.now());
    final UserDevice withoutToken = SecurityTestDataFactory.userDevice(user);
    withoutToken.setId(14L);
    withoutToken.setDeviceId("device-no-token");
    when(userSessionDeviceService.findActiveSessions(user))
        .thenReturn(List.of(current, other, expired, revoked, withoutToken));

    final List<UserSessionResponseDto> sessions = useCase.listMine();

    assertThat(sessions).extracting(UserSessionResponseDto::id).containsExactly(10L, 11L);
    assertThat(sessions).extracting(UserSessionResponseDto::current).containsExactly(true, false);
    assertThat(sessions.getFirst().userAgent()).isEqualTo(TestValues.USER_AGENT);
    assertThat(sessions.getFirst().ipAddress()).isEqualTo(TestValues.IP_ADDRESS);
  }

  @Test
  @DisplayName("Given another active session when it is closed then it is revoked with the user reason")
  void closesAnotherSession() {
    final UserDevice other = session(11L, TestValues.ALTERNATE_DEVICE_ID);
    when(userSessionDeviceService.findOwnedSession(11L, user)).thenReturn(other);

    useCase.closeMine(11L);

    verify(userSessionRevocationService)
        .close(user, other, UserSessionRevocationService.REASON_USER);
  }

  @Test
  @DisplayName("Given an already closed session when it is closed again then nothing is revoked")
  void closingAClosedSessionIsANoOp() {
    final UserDevice closed = session(11L, TestValues.ALTERNATE_DEVICE_ID);
    closed.setIsRefreshActive(Boolean.FALSE);
    when(userSessionDeviceService.findOwnedSession(11L, user)).thenReturn(closed);

    useCase.closeMine(11L);

    verify(userSessionRevocationService, never()).close(any(), any(), any());
  }

  @Test
  @DisplayName("Given the current session when it is closed then it is refused with a conflict")
  void refusesToCloseTheCurrentSession() {
    final UserDevice current = session(10L, TestValues.DEVICE_ID);
    when(userSessionDeviceService.findOwnedSession(10L, user)).thenReturn(current);

    assertThatThrownBy(() -> useCase.closeMine(10L))
        .isInstanceOf(ConflictException.class)
        .hasMessage("user.session.error.current");
    verify(userSessionRevocationService, never()).close(any(), any(), any());
  }

  @Test
  @DisplayName("Given a session of another user when it is closed then it is reported as not found")
  void anotherUsersSessionIsNotFound() {
    when(userSessionDeviceService.findOwnedSession(99L, user))
        .thenThrow(new NotFoundException("user.session.error.not.found"));

    assertThatThrownBy(() -> useCase.closeMine(99L)).isInstanceOf(NotFoundException.class);
    verify(userSessionRevocationService, never()).close(any(), any(), any());
  }

  @Test
  @DisplayName("Given several sessions when the others are closed then every one but the current is revoked")
  void closesEveryOtherSession() {
    when(userSessionRevocationService.closeOthers(
            user, TestValues.DEVICE_ID, UserSessionRevocationService.REASON_USER))
        .thenReturn(2);

    assertThat(useCase.closeOthers()).isEqualTo(2);
  }

  private UserDevice session(final Long id, final String deviceId) {
    final UserDevice device = SecurityTestDataFactory.userDevice(user);
    device.setId(id);
    device.setDeviceId(deviceId);
    device.setUserAgent(TestValues.USER_AGENT);
    device.setCreatedAt(Instant.now().minusSeconds(3600));
    SecurityTestDataFactory.refreshToken(device);
    return device;
  }
}
