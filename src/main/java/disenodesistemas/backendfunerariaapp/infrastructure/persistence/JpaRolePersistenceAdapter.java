package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.RolePersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.RoleEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.Role;
import disenodesistemas.backendfunerariaapp.persistence.repository.RoleRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaRolePersistenceAdapter implements RolePersistencePort {

  private final RoleRepository roleRepository;

  @Override
  public Optional<RoleEntity> findById(final Long id) {
    return roleRepository.findById(id);
  }

  @Override
  public Optional<RoleEntity> findByName(final Role role) {
    return roleRepository.findByName(role);
  }

  @Override
  public List<RoleEntity> findAll() {
    return roleRepository.findAll();
  }
}
