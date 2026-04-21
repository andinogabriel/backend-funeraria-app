package disenodesistemas.backendfunerariaapp.modern.application.usecase.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.UserPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserAuthenticationService;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import disenodesistemas.backendfunerariaapp.web.dto.request.UserLoginDto;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserAuthenticationService")
class UserAuthenticationServiceTest {

  @Mock private UserPersistencePort userPersistencePort;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserAuthenticationService userAuthenticationService;

  @Test
  @DisplayName(
      "Given a persisted active user and matching password when authentication is requested then it returns the authenticated user")
  void givenAPersistedActiveUserAndMatchingPasswordWhenAuthenticationIsRequestedThenItReturnsTheAuthenticatedUser() {
    final UserEntity userEntity = SecurityTestDataFactory.userEntity();
    final UserLoginDto loginDto =
        UserLoginDto.builder()
            .email(userEntity.getEmail())
            .password("password-123")
            .deviceInfo(SecurityTestDataFactory.deviceInfo())
            .build();

    when(userPersistencePort.findByEmail(userEntity.getEmail())).thenReturn(Optional.of(userEntity));
    when(passwordEncoder.matches(loginDto.password(), userEntity.getEncryptedPassword())).thenReturn(true);

    assertThat(userSessionAuthenticated(loginDto)).isEqualTo(userEntity);
  }

  @Test
  @DisplayName(
      "Given an unknown email when authentication is requested then it rejects the login as unauthorized")
  void givenAnUnknownEmailWhenAuthenticationIsRequestedThenItRejectsTheLoginAsUnauthorized() {
    final UserLoginDto loginDto =
        UserLoginDto.builder()
            .email("missing@example.com")
            .password("password-123")
            .deviceInfo(SecurityTestDataFactory.deviceInfo())
            .build();

    when(userPersistencePort.findByEmail(loginDto.email())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userAuthenticationService.authenticate(loginDto))
        .isInstanceOf(AppException.class)
        .extracting("message")
        .isEqualTo("user.error.email.not.registered");
  }

  @Test
  @DisplayName(
      "Given an inactive user when authentication is requested then it rejects the login with the business lock message")
  void givenAnInactiveUserWhenAuthenticationIsRequestedThenItRejectsTheLoginWithTheBusinessLockMessage() {
    final UserEntity userEntity = SecurityTestDataFactory.userEntity();
    userEntity.setActive(Boolean.FALSE);
    final UserLoginDto loginDto =
        UserLoginDto.builder()
            .email(userEntity.getEmail())
            .password("password-123")
            .deviceInfo(SecurityTestDataFactory.deviceInfo())
            .build();

    when(userPersistencePort.findByEmail(userEntity.getEmail())).thenReturn(Optional.of(userEntity));
    when(passwordEncoder.matches(loginDto.password(), userEntity.getEncryptedPassword())).thenReturn(true);

    assertThatThrownBy(() -> userAuthenticationService.authenticate(loginDto))
        .isInstanceOf(AppException.class)
        .extracting("message")
        .isEqualTo("user.error.deactivated.locked");
  }

  private UserEntity userSessionAuthenticated(final UserLoginDto loginDto) {
    return userAuthenticationService.authenticate(loginDto);
  }
}
