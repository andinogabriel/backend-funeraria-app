package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.BrandEntity;
import java.util.List;
import java.util.Optional;

public interface BrandPersistencePort {

  Optional<BrandEntity> findById(Long id);

  List<BrandEntity> findAllByOrderByName();

  BrandEntity save(BrandEntity brand);

  void delete(BrandEntity brand);
}
