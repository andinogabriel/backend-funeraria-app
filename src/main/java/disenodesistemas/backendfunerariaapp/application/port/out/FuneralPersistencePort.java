package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FuneralPersistencePort {

  Optional<Funeral> findById(Long id);

  List<Funeral> findAllByOrderByRegisterDateDesc();

  List<Funeral> findFuneralsByUserEmail(String userEmail);

  boolean existsByReceiptNumber(String receiptNumber);

  /**
   * Filtered + paginated read for the operator UI. Sentinel contract: callers pass
   * {@code ""} for inactive string filters and {@code null} for inactive date filters.
   */
  Page<Funeral> search(
      String deceasedName,
      String dni,
      String receiptNumber,
      String planName,
      LocalDateTime from,
      LocalDateTime to,
      Pageable pageable);

  Funeral save(Funeral funeral);

  void delete(Funeral funeral);
}
