package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.GenderEntity;
import java.util.List;
import java.util.Optional;

public interface GenderPersistencePort {

  Optional<GenderEntity> findById(Long id);

  List<GenderEntity> findAllByOrderByName();
}
