package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.RoleEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.Role;
import java.util.List;
import java.util.Optional;

public interface RolePersistencePort {

  Optional<RoleEntity> findById(Long id);

  Optional<RoleEntity> findByName(Role role);

  List<RoleEntity> findAll();
}
