package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IncomePersistencePort {

  Optional<IncomeEntity> findByReceiptNumber(Long receiptNumber);

  List<IncomeEntity> findAllByDeletedFalseOrderByIdDesc();

  Page<IncomeEntity> findAllByDeleted(boolean deleted, Pageable pageable);

  /**
   * Server-side filtered read with per-column predicates. Empty strings on
   * {@code receiptNumber} / {@code supplierNif} signal "no filter" so the adapter can use
   * the ADR-0010-compatible JPQL idiom; {@code null} on the {@code from} / {@code to}
   * bounds signal "open-ended". {@code receiptNumber} is matched case-insensitively as a
   * substring; {@code supplierNif} is matched exactly (the frontend feeds it from an
   * autocomplete-selected supplier).
   */
  Page<IncomeEntity> search(
      boolean deleted,
      String receiptNumber,
      String supplierNif,
      LocalDateTime from,
      LocalDateTime to,
      Pageable pageable);

  IncomeEntity save(IncomeEntity income);
}
