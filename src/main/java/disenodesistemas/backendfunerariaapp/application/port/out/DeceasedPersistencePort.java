package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.DeceasedEntity;
import java.util.List;
import java.util.Optional;

public interface DeceasedPersistencePort {

  Optional<DeceasedEntity> findByDni(Integer dni);

  List<DeceasedEntity> findAllByOrderByRegisterDateDesc();

  boolean existsByDni(Integer dni);

  DeceasedEntity save(DeceasedEntity deceased);

  void delete(DeceasedEntity deceased);
}
