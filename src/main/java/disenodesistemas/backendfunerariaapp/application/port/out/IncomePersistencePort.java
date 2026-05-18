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
   * Server-side filtered read. Empty strings on {@code q} / {@code supplierNif} signal "no
   * filter" so the adapter can use the ADR-0010-compatible JPQL idiom; {@code null} on the
   * {@code from} / {@code to} bounds signal "open-ended".
   */
  Page<IncomeEntity> search(
      boolean deleted,
      String q,
      String supplierNif,
      LocalDateTime from,
      LocalDateTime to,
      Pageable pageable);

  IncomeEntity save(IncomeEntity income);
}
