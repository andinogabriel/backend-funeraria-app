package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.ReceiptTypePersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.ReceiptTypeRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaReceiptTypePersistenceAdapter implements ReceiptTypePersistencePort {

  private final ReceiptTypeRepository receiptTypeRepository;

  @Override
  public List<ReceiptTypeEntity> findAllByOrderByName() {
    return receiptTypeRepository.findAllByOrderByName();
  }

  @Override
  public Optional<ReceiptTypeEntity> findByNameIsContainingIgnoreCase(final String name) {
    return receiptTypeRepository.findByNameIsContainingIgnoreCase(name);
  }
}
