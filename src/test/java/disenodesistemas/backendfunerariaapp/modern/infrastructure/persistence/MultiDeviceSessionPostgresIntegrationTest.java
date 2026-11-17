package disenodesistemas.backendfunerariaapp.modern.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import disenodesistemas.backendfunerariaapp.application.port.out.UserDevicePort;
import disenodesistemas.backendfunerariaapp.application.port.out.UserPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionRevocationService;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.RoleEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.Role;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.RoleRepository;
import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
import disenodesistemas.backendfunerariaapp.web.dto.DeviceInfo;
import disenodesistemas.backendfunerariaapp.web.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.TokenRefreshRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.UserLoginDto;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Multiple device sessions per user against the real schema (V23, ADR-0024): a second login no
 * longer replaces the first, a repeated login on the same device reuses its row, and closing the
 * other sessions leaves the kept one able to refresh.
 */
// Login signs an HS512 access token, which needs a key of at least 64 bytes.
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties =
        "jwt-token.secret=integration-test-jwt-secret-that-is-long-enough-for-hs512-signatures-0123456789")
class MultiDeviceSessionPostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  private static final String PASSWORD = "Integration-pass-123";

  @Autowired private UserSessionUseCase userSessionUseCase;
  @Autowired private UserSessionRevocationService userSessionRevocationService;
  @Autowired private UserDevicePort userDevicePort;
  @Autowired private UserPersistencePort userPersistencePort;
  @Autowired private RoleRepository roleRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private TransactionTemplate transactionTemplate;

  private String email;
  private DeviceInfo laptop;
  private DeviceInfo office;

  @BeforeEach
  void createUser() {
    final RoleEntity userRole = roleRepository.findByName(Role.ROLE_USER).orElseThrow();
    email = "sessions-" + UUID.randomUUID() + "@example.com";
    laptop = new DeviceInfo("laptop-" + UUID.randomUUID(), "BROWSER");
    office = new DeviceInfo("office-" + UUID.randomUUID(), "BROWSER");
    final UserEntity user =
        new UserEntity(email, "Multi", "Device", passwordEncoder.encode(PASSWORD));
    user.setEnabled(true);
    user.activate();
    user.setRoles(Set.of(userRole));
    userPersistencePort.save(user);
  }

  @Test
  @DisplayName(
      "Given logins from two devices when both refresh then both sessions stay active side by side")
  void twoDevicesKeepTheirOwnSessions() {
    final String laptopToken = login(laptop).refreshToken();
    final String officeToken = login(office).refreshToken();

    assertThat(refresh(laptopToken, laptop).refreshToken()).isNotBlank();
    assertThat(refresh(officeToken, office).refreshToken()).isNotBlank();
    assertThat(activeDeviceIds()).containsExactlyInAnyOrder(laptop.deviceId(), office.deviceId());
    assertThat(sessionRows()).isEqualTo(2);
  }

  @Test
  @DisplayName("Given a device that logs in twice when the sessions are counted then its row is reused")
  void repeatedLoginOnTheSameDeviceReusesItsRow() {
    login(laptop);
    login(laptop);

    assertThat(sessionRows()).isEqualTo(1);
  }

  @Test
  @DisplayName(
      "Given sessions on two devices when the others are closed then only the kept device can refresh")
  void closingTheOthersKeepsOnlyTheCurrentDevice() {
    final String laptopToken = login(laptop).refreshToken();
    final String officeToken = login(office).refreshToken();

    final Integer closed =
        transactionTemplate.execute(
            status ->
                userSessionRevocationService.closeOthers(
                    userPersistencePort.findByEmail(email).orElseThrow(),
                    laptop.deviceId(),
                    UserSessionRevocationService.REASON_USER));

    assertThat(closed).isEqualTo(1);
    assertThat(activeDeviceIds()).containsExactly(laptop.deviceId());
    assertThat(refresh(laptopToken, laptop).refreshToken()).isNotBlank();
    assertThatThrownBy(() -> refresh(officeToken, office)).hasMessage("refresh.token.error.revoked");
    assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from audit_events where action = 'USER_SESSION_REVOKED' "
                    + "and actor_email = ?",
                Integer.class,
                email))
        .isEqualTo(1);
  }

  @Test
  @DisplayName(
      "Given a device id already used by another user when this user logs in from it then both sessions exist")
  void theSameDeviceIdMayBelongToDifferentUsers() {
    login(laptop);
    final String otherEmail = "sessions-other-" + UUID.randomUUID() + "@example.com";
    final UserEntity other =
        new UserEntity(otherEmail, "Other", "User", passwordEncoder.encode(PASSWORD));
    other.setEnabled(true);
    other.activate();
    other.setRoles(Set.of(roleRepository.findByName(Role.ROLE_USER).orElseThrow()));
    userPersistencePort.save(other);

    userSessionUseCase.login(
        UserLoginDto.builder().email(otherEmail).password(PASSWORD).deviceInfo(laptop).build());

    assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from user_devices where device_id = ?",
                Integer.class,
                laptop.deviceId()))
        .isEqualTo(2);
  }

  private JwtDto login(final DeviceInfo device) {
    return userSessionUseCase.login(
        UserLoginDto.builder().email(email).password(PASSWORD).deviceInfo(device).build());
  }

  private JwtDto refresh(final String rawRefreshToken, final DeviceInfo device) {
    return userSessionUseCase.refreshJwtToken(
        rawRefreshToken, TokenRefreshRequestDto.builder().deviceInfo(device).build());
  }

  private List<String> activeDeviceIds() {
    return transactionTemplate.execute(
        status ->
            userDevicePort.findActiveByUser(userPersistencePort.findByEmail(email).orElseThrow()).stream()
                .map(UserDevice::getDeviceId)
                .toList());
  }

  private Integer sessionRows() {
    return jdbcTemplate.queryForObject(
        "select count(*) from user_devices d join users u on u.id = d.user_id where u.email = ?",
        Integer.class,
        email);
  }
}
