package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.SupplierEntity;
import java.util.List;
import java.util.Optional;

public interface SupplierPersistencePort {

  Optional<SupplierEntity> findByNif(String nif);

  List<SupplierEntity> findAllByOrderByIdDesc();

  SupplierEntity save(SupplierEntity supplier);

  void delete(SupplierEntity supplier);
}
