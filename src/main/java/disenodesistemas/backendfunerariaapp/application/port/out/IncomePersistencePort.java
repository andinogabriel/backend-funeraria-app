package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IncomePersistencePort {

  Optional<IncomeEntity> findByReceiptNumber(Long receiptNumber);

  List<IncomeEntity> findAllByDeletedFalseOrderByIdDesc();

  Page<IncomeEntity> findAllByDeleted(boolean deleted, Pageable pageable);

  IncomeEntity save(IncomeEntity income);
}
