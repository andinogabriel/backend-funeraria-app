package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.IncomeStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IncomePersistencePort {

  /** Primary-key lookup used by the annul flow to load the original receipt by id. */
  Optional<IncomeEntity> findById(Long id);

  Optional<IncomeEntity> findByReceiptNumber(Long receiptNumber);

  /** Lists every {@code ACTIVE} income, most-recent first. Excludes annulled rows. */
  List<IncomeEntity> findAllActiveOrderByIdDesc();

  /**
   * Server-side filtered read with per-column predicates. Empty strings on
   * {@code receiptNumber} / {@code supplierNif} signal "no filter" so the adapter can use
   * the ADR-0010-compatible JPQL idiom; {@code null} on the {@code from} / {@code to}
   * bounds signal "open-ended". A {@code null} {@code status} returns every row regardless
   * of lifecycle state (used by the "Todas" filter on the operator UI).
   */
  Page<IncomeEntity> search(
      IncomeStatus status,
      String receiptNumber,
      String supplierNif,
      Instant from,
      Instant to,
      Pageable pageable);

  IncomeEntity save(IncomeEntity income);
}
