package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.dto.response.IncomeResponseDto;
import disenodesistemas.backendfunerariaapp.entities.IncomeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeRepository extends PagingAndSortingRepository<IncomeEntity, Long> {

    Optional<IncomeEntity> findByReceiptNumber(Long receiptNumber);

    boolean existsByReceiptNumber(Long receiptNumber);

    List<IncomeResponseDto> findAllByOrderByIdDesc();

    Page<IncomeResponseDto> findAllProjectedBy(Pageable pageable);

}
