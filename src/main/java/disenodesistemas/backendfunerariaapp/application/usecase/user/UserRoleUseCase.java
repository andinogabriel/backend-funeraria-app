package disenodesistemas.backendfunerariaapp.application.usecase.user;

import disenodesistemas.backendfunerariaapp.application.port.out.RolePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.UserPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.RoleEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
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

  private final UserPersistencePort userPersistencePort;
  private final RolePersistencePort rolePersistencePort;
  private final RoleMapper roleMapper;

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
    }

    return user.getRoles().stream()
        .map(roleMapper::toRequestDto)
        .collect(Collectors.toUnmodifiableSet());
  }

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
}
