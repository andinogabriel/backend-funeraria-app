package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.DeathCauseEntity;
import java.util.List;
import java.util.Optional;

public interface DeathCausePersistencePort {

  Optional<DeathCauseEntity> findById(Long id);

  List<DeathCauseEntity> findAllByOrderByNameAsc();

  DeathCauseEntity save(DeathCauseEntity deathCause);

  void delete(DeathCauseEntity deathCause);
}
