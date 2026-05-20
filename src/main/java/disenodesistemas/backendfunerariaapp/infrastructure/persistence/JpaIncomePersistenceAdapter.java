package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.IncomePersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.IncomeRepository;
import java.time.LocalDateTime;
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
  public Optional<IncomeEntity> findByReceiptNumber(final Long receiptNumber) {
    return incomeRepository.findByReceiptNumber(receiptNumber);
  }

  @Override
  public List<IncomeEntity> findAllByDeletedFalseOrderByIdDesc() {
    return incomeRepository.findAllByDeletedFalseOrderByIdDesc();
  }

  @Override
  public Page<IncomeEntity> findAllByDeleted(final boolean deleted, final Pageable pageable) {
    return incomeRepository.findAllByDeleted(deleted, pageable);
  }

  @Override
  public Page<IncomeEntity> search(
      final boolean deleted,
      final String receiptNumber,
      final String supplierNif,
      final LocalDateTime from,
      final LocalDateTime to,
      final Pageable pageable) {
    return incomeRepository.search(deleted, receiptNumber, supplierNif, from, to, pageable);
  }

  @Override
  @Transactional
  public IncomeEntity save(final IncomeEntity income) {
    return incomeRepository.save(income);
  }
}
