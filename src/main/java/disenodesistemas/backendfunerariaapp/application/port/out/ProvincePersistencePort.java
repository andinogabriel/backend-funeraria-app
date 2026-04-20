package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.ProvinceEntity;
import java.util.List;
import java.util.Optional;

public interface ProvincePersistencePort {

  Optional<ProvinceEntity> findById(Long id);

  List<ProvinceEntity> findAllByOrderByName();
}
