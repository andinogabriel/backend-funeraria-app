package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import java.util.List;
import java.util.Optional;

public interface FuneralPersistencePort {

  Optional<Funeral> findById(Long id);

  List<Funeral> findAllByOrderByRegisterDateDesc();

  List<Funeral> findFuneralsByUserEmail(String userEmail);

  boolean existsByReceiptNumber(String receiptNumber);

  Funeral save(Funeral funeral);

  void delete(Funeral funeral);
}
