package disenodesistemas.backendfunerariaapp.modern.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import disenodesistemas.backendfunerariaapp.application.port.out.UserPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.RoleEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.Role;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.exception.RefreshTokenReuseException;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.RoleRepository;
import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
import disenodesistemas.backendfunerariaapp.web.dto.DeviceInfo;
import disenodesistemas.backendfunerariaapp.web.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.TokenRefreshRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.UserLoginDto;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Exercises refresh-token reuse detection against the real schema (V22) and real transactions.
 * The unit tests cover the branching; this test proves what they cannot: that the revocation
 * performed right before {@link RefreshTokenReuseException} is thrown is committed, not rolled
 * back with the failed request.
 */
// Login signs an HS512 access token, which needs a key of at least 64 bytes; the default
// placeholder secret is shorter because no other integration test signs a JWT.
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties =
        "jwt-token.secret=integration-test-jwt-secret-that-is-long-enough-for-hs512-signatures-0123456789")
class RefreshTokenReusePostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  private static final String PASSWORD = "Integration-pass-123";

  @Autowired private UserSessionUseCase userSessionUseCase;
  @Autowired private UserPersistencePort userPersistencePort;
  @Autowired private RoleRepository roleRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private JdbcTemplate jdbcTemplate;

  private String email;
  private DeviceInfo deviceInfo;

  @BeforeEach
  void createUser() {
    final RoleEntity userRole = roleRepository.findByName(Role.ROLE_USER).orElseThrow();
    email = "refresh-" + UUID.randomUUID() + "@example.com";
    deviceInfo = new DeviceInfo("device-" + UUID.randomUUID(), "BROWSER");
    final UserEntity user =
        new UserEntity(email, "Refresh", "Reuse", passwordEncoder.encode(PASSWORD));
    user.setEnabled(true);
    user.activate();
    user.setRoles(Set.of(userRole));
    userPersistencePort.save(user);
  }

  @Test
  @DisplayName(
      "Given a rotated refresh token when it is replayed within the grace window then it answers 409 and the session keeps working")
  void replayWithinGraceIsABenignRace() {
    final String first = login().refreshToken();
    final String second = refresh(first).refreshToken();

    assertThatThrownBy(() -> refresh(first)).isInstanceOf(ConflictException.class);

    assertThat(refresh(second).refreshToken()).isNotBlank();
    assertThat(sessionRevoked()).isFalse();
  }

  @Test
  @DisplayName(
      "Given a rotated refresh token when it is replayed after the grace window then the device session is revoked, audited and stays revoked")
  void replayAfterGraceRevokesTheSession() {
    final String first = login().refreshToken();
    final String second = refresh(first).refreshToken();
    jdbcTemplate.update(
        "update refresh_tokens set rotated_at = rotated_at - interval '1 hour' "
            + "where user_device_id = (select id from user_devices where device_id = ?)",
        deviceInfo.deviceId());

    assertThatThrownBy(() -> refresh(first)).isInstanceOf(RefreshTokenReuseException.class);

    assertThat(sessionRevoked()).isTrue();
    assertThat(
            jdbcTemplate.queryForObject(
                "select is_refresh_active from user_devices where device_id = ?",
                Boolean.class,
                deviceInfo.deviceId()))
        .isFalse();
    assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from audit_events where action = 'REFRESH_TOKEN_REUSE_DETECTED' "
                    + "and actor_email = ?",
                Integer.class,
                email))
        .isEqualTo(1);
    // The holder of the newest token is logged out too: the session itself is gone.
    assertThatThrownBy(() -> refresh(second))
        .hasMessage("refresh.token.error.revoked");
  }

  private JwtDto login() {
    return userSessionUseCase.login(
        UserLoginDto.builder().email(email).password(PASSWORD).deviceInfo(deviceInfo).build());
  }

  private JwtDto refresh(final String rawRefreshToken) {
    return userSessionUseCase.refreshJwtToken(
        rawRefreshToken, TokenRefreshRequestDto.builder().deviceInfo(deviceInfo).build());
  }

  private boolean sessionRevoked() {
    return jdbcTemplate.queryForObject(
        "select revoked_at is not null from refresh_tokens "
            + "where user_device_id = (select id from user_devices where device_id = ?)",
        Boolean.class,
        deviceInfo.deviceId());
  }
}
