package disenodesistemas.backendfunerariaapp.modern.application.usecase.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.ConfirmationTokenPort;
import disenodesistemas.backendfunerariaapp.application.port.out.MessageResolverPort;
import disenodesistemas.backendfunerariaapp.application.port.out.RolePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.UserPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserAccountUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.ConfirmationTokenEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.RoleEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.Role;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.UserMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.UserRegisterDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.UserResponseDto;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("UserAccountUseCase")
class UserAccountUseCaseTest {

  @Mock private UserPersistencePort userPersistencePort;
  @Mock private RolePersistencePort rolePersistencePort;
  @Mock private UserMapper userMapper;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private ConfirmationTokenPort confirmationTokenPort;
  @Mock private MessageResolverPort messageResolverPort;
  @Mock private disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort auditEventPort;

  @InjectMocks private UserAccountUseCase userAccountUseCase;

  @Test
  @DisplayName(
      "Given a new email when the user account is created then it encodes the password, assigns the default role and persists the activated account")
  void givenANewEmailWhenTheUserAccountIsCreatedThenItEncodesThePasswordAssignsTheDefaultRoleAndPersistsTheActivatedAccount() {
    final UserRegisterDto registerDto = userRegisterDto();
    final UserEntity mappedUser =
        new UserEntity(registerDto.email(), registerDto.firstName(), registerDto.lastName(), null);
    final RoleEntity defaultRole = new RoleEntity(Role.ROLE_USER);
    final UserResponseDto expectedResponse =
        new UserResponseDto(
            1L,
            registerDto.firstName(),
            registerDto.lastName(),
            registerDto.email(),
            Boolean.TRUE,
            null,
            false,
            List.of(),
            List.of(),
            Set.of());

    when(userPersistencePort.findByEmail(registerDto.email())).thenReturn(Optional.empty());
    when(userMapper.toRegisterEntity(registerDto)).thenReturn(mappedUser);
    when(passwordEncoder.encode(registerDto.password())).thenReturn("argon2-hash");
    when(rolePersistencePort.findByName(Role.ROLE_USER)).thenReturn(Optional.of(defaultRole));
    when(userPersistencePort.save(mappedUser)).thenReturn(mappedUser);
    when(userMapper.toDto(mappedUser)).thenReturn(expectedResponse);

    final UserResponseDto response = userAccountUseCase.createUser(registerDto);

    assertThat(response).isEqualTo(expectedResponse);
    assertThat(mappedUser.getEncryptedPassword()).isEqualTo("argon2-hash");
    assertThat(mappedUser.getRoles()).containsExactly(defaultRole);
    assertThat(mappedUser.getActive()).isTrue();
    verify(userPersistencePort).save(mappedUser);
  }

  @Test
  @DisplayName(
      "Given an already registered email when the user account is created then it rejects the request as a conflict")
  void givenAnAlreadyRegisteredEmailWhenTheUserAccountIsCreatedThenItRejectsTheRequestAsAConflict() {
    final UserRegisterDto registerDto = userRegisterDto();

    when(userPersistencePort.findByEmail(registerDto.email()))
        .thenReturn(Optional.of(new UserEntity()));

    assertThatThrownBy(() -> userAccountUseCase.createUser(registerDto))
        .isInstanceOf(ConflictException.class)
        .extracting("message")
        .isEqualTo("user.error.email.already.registered");

    verify(userMapper, never()).toRegisterEntity(registerDto);
  }

  @Test
  @DisplayName(
      "Given a valid confirmation token when the account is confirmed then it enables the user and returns the localized success message")
  void givenAValidConfirmationTokenWhenTheAccountIsConfirmedThenItEnablesTheUserAndReturnsTheLocalizedSuccessMessage() {
    final UserEntity userEntity =
        new UserEntity("john.doe@example.com", "John", "Doe", "argon2-hash");
    userEntity.setEnabled(false);
    final ConfirmationTokenEntity tokenEntity = new ConfirmationTokenEntity(userEntity, "token-123");
    tokenEntity.setExpiryDate(Instant.now().plusSeconds(300));

    when(confirmationTokenPort.findByToken("token-123")).thenReturn(tokenEntity);
    when(messageResolverPort.getMessage("confirmationToken.successful.activation"))
        .thenReturn("Cuenta activada correctamente");

    final String response = userAccountUseCase.confirmationUser("token-123");

    assertThat(response).isEqualTo("Cuenta activada correctamente");
    assertThat(userEntity.isEnabled()).isTrue();
    verify(userPersistencePort).save(userEntity);
    verify(auditEventPort)
        .record(
            disenodesistemas.backendfunerariaapp.domain.enums.AuditAction.USER_ACTIVATED,
            userEntity.getEmail(),
            userEntity.getId(),
            "USER",
            String.valueOf(userEntity.getId()),
            null);
  }

  @Test
  @DisplayName(
      "Given an expired confirmation token when the account is confirmed then it returns the localized expiration message without persisting changes")
  void givenAnExpiredConfirmationTokenWhenTheAccountIsConfirmedThenItReturnsTheLocalizedExpirationMessageWithoutPersistingChanges() {
    final UserEntity userEntity =
        new UserEntity("john.doe@example.com", "John", "Doe", "argon2-hash");
    userEntity.setEnabled(false);
    final ConfirmationTokenEntity tokenEntity = new ConfirmationTokenEntity(userEntity, "token-123");
    tokenEntity.setExpiryDate(Instant.now().minusSeconds(60));

    when(confirmationTokenPort.findByToken("token-123")).thenReturn(tokenEntity);
    when(messageResolverPort.getMessage("confirmationToken.error.expired"))
        .thenReturn("El enlace de activacion expiro");

    final String response = userAccountUseCase.confirmationUser("token-123");

    assertThat(response).isEqualTo("El enlace de activacion expiro");
    assertThat(userEntity.isEnabled()).isFalse();
    verify(userPersistencePort, never()).save(userEntity);
  }

  private UserRegisterDto userRegisterDto() {
    return UserRegisterDto.builder()
        .firstName("John")
        .lastName("Doe")
        .email("john.doe@example.com")
        .password("Password-123")
        .matchingPassword("Password-123")
        .build();
  }
}
