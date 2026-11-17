package disenodesistemas.backendfunerariaapp.modern.application.usecase.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionDeviceService;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionRevocationService;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionTokenService;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import disenodesistemas.backendfunerariaapp.modern.support.TestValues;
import java.util.List;
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
@DisplayName("UserSessionRevocationService")
class UserSessionRevocationServiceTest {

  @Mock private UserSessionDeviceService userSessionDeviceService;
  @Mock private UserSessionTokenService userSessionTokenService;
  @Mock private AuditEventPort auditEventPort;

  @InjectMocks private UserSessionRevocationService service;

  @Test
  @DisplayName("Given a session when it is closed then it is deactivated, its token revoked and the closure audited")
  void closesAndAudits() {
    final UserEntity user = SecurityTestDataFactory.userEntity();
    final UserDevice session = SecurityTestDataFactory.userDevice(user);
    when(userSessionDeviceService.deactivateSession(session)).thenReturn(session);

    service.close(user, session, UserSessionRevocationService.REASON_USER);

    verify(userSessionTokenService).revokeIfPresent(session);
    verify(auditEventPort)
        .record(
            AuditAction.USER_SESSION_REVOKED,
            user.getEmail(),
            user.getId(),
            "USER_DEVICE",
            "10",
            "{\"reason\":\"user\"}");
  }

  @Test
  @DisplayName("Given sessions on three devices when the others are closed then the kept device survives")
  void closesEveryOtherSession() {
    final UserEntity user = SecurityTestDataFactory.userEntity();
    final UserDevice kept = device(user, 10L, TestValues.DEVICE_ID);
    final UserDevice second = device(user, 11L, TestValues.ALTERNATE_DEVICE_ID);
    final UserDevice third = device(user, 12L, "device-third");
    when(userSessionDeviceService.findActiveSessions(user)).thenReturn(List.of(kept, second, third));
    when(userSessionDeviceService.deactivateSession(any())).thenAnswer(call -> call.getArgument(0));

    final int closed =
        service.closeOthers(
            user, TestValues.DEVICE_ID, UserSessionRevocationService.REASON_PASSWORD_CHANGE);

    assertThat(closed).isEqualTo(2);
    verify(userSessionDeviceService).deactivateSession(second);
    verify(userSessionDeviceService).deactivateSession(third);
    verify(userSessionDeviceService, never()).deactivateSession(kept);
  }

  @org.junit.jupiter.params.ParameterizedTest
  @org.junit.jupiter.params.provider.NullAndEmptySource
  @org.junit.jupiter.params.provider.ValueSource(strings = {"  "})
  @DisplayName("Given no current device id when the others are closed then nothing is closed and it fails")
  void refusesToCloseWithoutTheCurrentDevice(final String keepDeviceId) {
    final UserEntity user = SecurityTestDataFactory.userEntity();

    org.assertj.core.api.Assertions.assertThatThrownBy(
            () -> service.closeOthers(user, keepDeviceId, UserSessionRevocationService.REASON_USER))
        .hasMessage("security.request.device.header.required");
    verify(userSessionDeviceService, never()).findActiveSessions(any());
  }

  @Test
  @DisplayName("Given more active sessions than the limit when a login is capped then the oldest are closed")
  void closesTheOldestSessionsBeyondTheLimit() {
    final UserEntity user = SecurityTestDataFactory.userEntity();
    final List<UserDevice> sessions =
        java.util.stream.LongStream.rangeClosed(1, UserSessionRevocationService.MAX_ACTIVE_SESSIONS + 2)
            .mapToObj((final long id) -> device(user, id, "device-" + id))
            .toList();
    when(userSessionDeviceService.findActiveSessions(user)).thenReturn(sessions);
    when(userSessionDeviceService.deactivateSession(any())).thenAnswer(call -> call.getArgument(0));

    assertThat(service.closeBeyondLimit(user)).isEqualTo(2);
    verify(userSessionDeviceService).deactivateSession(sessions.get(10));
    verify(userSessionDeviceService).deactivateSession(sessions.get(11));
    verify(userSessionDeviceService, never()).deactivateSession(sessions.getFirst());
  }

  @Test
  @DisplayName("Given sessions within the limit when a login is capped then nothing is closed")
  void keepsSessionsWithinTheLimit() {
    final UserEntity user = SecurityTestDataFactory.userEntity();
    when(userSessionDeviceService.findActiveSessions(user))
        .thenReturn(List.of(device(user, 1L, "device-1")));

    assertThat(service.closeBeyondLimit(user)).isZero();
    verify(userSessionDeviceService, never()).deactivateSession(any());
  }

  private static UserDevice device(final UserEntity user, final Long id, final String deviceId) {
    final UserDevice device = SecurityTestDataFactory.userDevice(user);
    device.setId(id);
    device.setDeviceId(deviceId);
    return device;
  }
}
