package disenodesistemas.backendfunerariaapp.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncomeRepository extends JpaRepository<IncomeEntity, Long> {
  Optional<IncomeEntity> findByReceiptNumber(Long receiptNumber);

  List<IncomeEntity> findAllByOrderByIdDesc();

  List<IncomeEntity> findAllByDeletedFalseOrderByIdDesc();

  Page<IncomeEntity> findAllByDeleted(boolean deleted, Pageable pageable);
}
