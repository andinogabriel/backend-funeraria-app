package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.ReceiptTypeEntity;
import java.util.List;
import java.util.Optional;

public interface ReceiptTypePersistencePort {

  List<ReceiptTypeEntity> findAllByOrderByName();

  Optional<ReceiptTypeEntity> findByNameIsContainingIgnoreCase(String name);
}
