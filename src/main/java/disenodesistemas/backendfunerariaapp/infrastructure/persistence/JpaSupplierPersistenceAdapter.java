package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.SupplierPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.SupplierEntity;
import disenodesistemas.backendfunerariaapp.persistence.repository.SupplierRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaSupplierPersistenceAdapter implements SupplierPersistencePort {

  private final SupplierRepository supplierRepository;

  @Override
  public Optional<SupplierEntity> findByNif(final String nif) {
    return supplierRepository.findByNif(nif);
  }

  @Override
  public List<SupplierEntity> findAllByOrderByIdDesc() {
    return supplierRepository.findAllByOrderByIdDesc();
  }

  @Override
  @Transactional
  public SupplierEntity save(final SupplierEntity supplier) {
    return supplierRepository.save(supplier);
  }

  @Override
  @Transactional
  public void delete(final SupplierEntity supplier) {
    supplierRepository.delete(supplier);
  }
}
