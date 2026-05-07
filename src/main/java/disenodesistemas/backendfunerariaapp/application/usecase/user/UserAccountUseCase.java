package disenodesistemas.backendfunerariaapp.application.usecase.user;

import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.application.port.out.ConfirmationTokenPort;
import disenodesistemas.backendfunerariaapp.application.port.out.MessageResolverPort;
import disenodesistemas.backendfunerariaapp.application.port.out.RolePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.UserPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.ConfirmationTokenEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.RoleEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.domain.enums.Role;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.UserMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.UserRegisterDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.UserResponseDto;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserAccountUseCase {

  private static final String AUDIT_TARGET_TYPE = "USER";

  private final UserPersistencePort userPersistencePort;
  private final RolePersistencePort rolePersistencePort;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final ConfirmationTokenPort confirmationTokenPort;
  private final MessageResolverPort messageResolverPort;
  private final AuditEventPort auditEventPort;

  @Transactional
  public UserResponseDto createUser(final UserRegisterDto user) {
    if (userPersistencePort.findByEmail(user.email()).isPresent()) {
      log.atWarn()
          .addKeyValue("event", "user.account.create.rejected")
          .addKeyValue("email", user.email())
          .addKeyValue("reason", "email_already_registered")
          .log("user.account.create.rejected");
      throw new ConflictException("user.error.email.already.registered");
    }

    log.atInfo()
        .addKeyValue("event", "user.account.create.started")
        .addKeyValue("email", user.email())
        .log("user.account.create.started");
    final UserEntity userEntity = userMapper.toRegisterEntity(user);
    userEntity.setEncryptedPassword(passwordEncoder.encode(user.password()));
    userEntity.setRoles(getDefaultRoles());
    userEntity.activate();
    final UserResponseDto createdUser = userMapper.toDto(userPersistencePort.save(userEntity));
    log.atInfo()
        .addKeyValue("event", "user.account.create.completed")
        .addKeyValue("email", createdUser.email())
        .addKeyValue("userId", createdUser.id())
        .log("user.account.create.completed");
    return createdUser;
  }

  @Transactional
  public String confirmationUser(final String token) {
    log.atInfo()
        .addKeyValue("event", "user.account.confirmation.started")
        .log("user.account.confirmation.started");
    final ConfirmationTokenEntity tokenEntity = confirmationTokenPort.findByToken(token);
    final UserEntity userEntity = tokenEntity.getUser();

    if (userEntity.isEnabled()) {
      log.atWarn()
          .addKeyValue("event", "user.account.confirmation.rejected")
          .addKeyValue("email", userEntity.getEmail())
          .addKeyValue("reason", "already_activated")
          .log("user.account.confirmation.rejected");
      throw new AppException(
          "confirmationToken.error.already.activated", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    if (tokenEntity.getExpiryDate().isBefore(Instant.now())) {
      log.atWarn()
          .addKeyValue("event", "user.account.confirmation.rejected")
          .addKeyValue("email", userEntity.getEmail())
          .addKeyValue("reason", "expired_token")
          .log("user.account.confirmation.rejected");
      return messageResolverPort.getMessage("confirmationToken.error.expired");
    }

    userEntity.setEnabled(true);
    userPersistencePort.save(userEntity);
    log.atInfo()
        .addKeyValue("event", "user.account.confirmation.completed")
        .addKeyValue("email", userEntity.getEmail())
        .log("user.account.confirmation.completed");
    recordUserActivated(userEntity);
    return messageResolverPort.getMessage("confirmationToken.successful.activation");
  }

  /**
   * Emits the {@link AuditAction#USER_ACTIVATED} entry for a successful email confirmation.
   * The actor is the user being activated themselves: this flow runs from the unauthenticated
   * confirmation link, so there is no admin session to read from
   * {@code AuthenticatedUserPort}; recording the user as their own actor still produces a
   * useful trail keyed on the email and id pair.
   */
  private void recordUserActivated(final UserEntity userEntity) {
    auditEventPort.record(
        AuditAction.USER_ACTIVATED,
        userEntity.getEmail(),
        userEntity.getId(),
        AUDIT_TARGET_TYPE,
        String.valueOf(userEntity.getId()),
        null);
  }

  private Set<RoleEntity> getDefaultRoles() {
    final Set<RoleEntity> roles = new HashSet<>();
    rolePersistencePort.findByName(Role.ROLE_USER).ifPresent(roles::add);
    return roles;
  }
}
