package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.IncomePersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.IncomeStatus;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.IncomeRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaIncomePersistenceAdapter implements IncomePersistencePort {

  private final IncomeRepository incomeRepository;

  @Override
  public Optional<IncomeEntity> findById(final Long id) {
    return incomeRepository.findById(id);
  }

  @Override
  public Optional<IncomeEntity> findByReceiptNumber(final Long receiptNumber) {
    return incomeRepository.findByReceiptNumber(receiptNumber);
  }

  @Override
  public List<IncomeEntity> findAllActiveOrderByIdDesc() {
    return incomeRepository.findAllActiveOrderByIdDesc();
  }

  @Override
  public Page<IncomeEntity> search(
      final IncomeStatus status,
      final String receiptNumber,
      final String supplierNif,
      final Instant from,
      final Instant to,
      final Pageable pageable) {
    return incomeRepository.search(status, receiptNumber, supplierNif, from, to, pageable);
  }

  @Override
  @Transactional
  public IncomeEntity save(final IncomeEntity income) {
    return incomeRepository.save(income);
  }
}
