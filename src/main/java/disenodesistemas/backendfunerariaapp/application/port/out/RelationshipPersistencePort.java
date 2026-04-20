package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.RelationshipEntity;
import java.util.List;
import java.util.Optional;

public interface RelationshipPersistencePort {

  Optional<RelationshipEntity> findById(Long id);

  List<RelationshipEntity> findAllByOrderByName();
}
