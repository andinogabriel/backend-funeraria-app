package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.CityEntity;
import java.util.List;
import java.util.Optional;

public interface CityPersistencePort {

  Optional<CityEntity> findById(Long id);

  List<CityEntity> findByProvinceIdOrderByName(Long provinceId);
}
