package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.IncomePersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import disenodesistemas.backendfunerariaapp.persistence.repository.IncomeRepository;
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
  @Transactional
  public IncomeEntity save(final IncomeEntity income) {
    return incomeRepository.save(income);
  }
}
