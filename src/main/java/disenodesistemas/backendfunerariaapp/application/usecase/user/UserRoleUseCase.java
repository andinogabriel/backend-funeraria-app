package disenodesistemas.backendfunerariaapp.application.usecase.user;

import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.RolePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.UserPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.RoleEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.RoleMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.RolRequestDto;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRoleUseCase {

  private static final String AUDIT_TARGET_TYPE = "USER";

  private final UserPersistencePort userPersistencePort;
  private final RolePersistencePort rolePersistencePort;
  private final RoleMapper roleMapper;
  private final AuthenticatedUserPort authenticatedUserPort;
  private final AuditEventPort auditEventPort;

  /**
   * Ensures the supplied role is part of the target user's role set. The operation is
   * idempotent: if the user already has the role nothing is persisted and no audit entry is
   * recorded; only an actual addition triggers a {@link AuditAction#USER_ROLE_GRANTED} event,
   * stamped with the admin who performed the change as actor and the target user's id as
   * audit target. Returns the resulting role set so callers can confirm the new state without
   * a follow-up read.
   */
  @Transactional
  public Set<RolRequestDto> updateUserRol(final String email, final RolRequestDto rolRequestDto) {
    final UserEntity user =
        userPersistencePort
            .findByEmail(email)
            .orElseThrow(() -> new NotFoundException("user.error.email.not.registered"));

    final RoleEntity roleEntity = resolveRole(rolRequestDto);
    final boolean alreadyAssigned =
        user.getRoles().stream().anyMatch(role -> Objects.equals(role.getId(), roleEntity.getId()));

    if (!alreadyAssigned) {
      user.addRol(roleEntity);
      userPersistencePort.save(user);
      recordRoleGranted(user, roleEntity);
    }

    return user.getRoles().stream()
        .map(roleMapper::toRequestDto)
        .collect(Collectors.toUnmodifiableSet());
  }

  /**
   * Resolves the role to apply, accepting either the database id or the role name. The
   * caller-provided DTO uses the id when known and falls back to the name for the well-known
   * default roles, so this method centralizes the lookup branching.
   */
  private RoleEntity resolveRole(final RolRequestDto rolRequestDto) {
    if (rolRequestDto.id() != null) {
      return rolePersistencePort
          .findById(rolRequestDto.id())
          .orElseThrow(() -> new NotFoundException("role.error.id.not.found"));
    }

    return rolePersistencePort
        .findByName(rolRequestDto.name())
        .orElseThrow(() -> new NotFoundException("role.error.name.not.found"));
  }

  /**
   * Emits the audit entry that captures the role grant. Builds a small JSON-shaped payload
   * with the role name so compliance reports can group grants by role without reaching back
   * into the role table from the audit query path.
   */
  private void recordRoleGranted(final UserEntity user, final RoleEntity roleEntity) {
    final UserEntity actor = authenticatedUserPort.getAuthenticatedUser();
    final String payload = "{\"role\":\"" + roleEntity.getName().name() + "\"}";
    auditEventPort.record(
        AuditAction.USER_ROLE_GRANTED,
        actor.getEmail(),
        actor.getId(),
        AUDIT_TARGET_TYPE,
        String.valueOf(user.getId()),
        payload);
  }
}
