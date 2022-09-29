package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.dto.response.IncomeResponseDto;
import disenodesistemas.backendfunerariaapp.entities.IncomeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeRepository extends PagingAndSortingRepository<IncomeEntity, Long> {

    Optional<IncomeEntity> findByReceiptNumber(Long receiptNumber);

    @Modifying
    @Query("UPDATE incomes i SET i.deleted = TRUE WHERE i.receiptNumber =:receiptNumber")
    void deleteByReceiptNumber(@Param("receiptNumber") Long receiptNumber);

    boolean existsByReceiptNumber(Long receiptNumber);
    List<IncomeEntity> findByReceiptNumberOrderByIdDesc(Long receiptNumber);
    List<IncomeResponseDto> findAllByOrderByIdDesc();
    List<IncomeResponseDto> findByDeletedFalseOrderByIdDesc();

    Page<IncomeResponseDto> findAllProjectedBy(Pageable pageable);

}
