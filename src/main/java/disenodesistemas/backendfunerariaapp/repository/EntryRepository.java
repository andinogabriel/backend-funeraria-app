package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.dto.response.EntryResponseDto;
import disenodesistemas.backendfunerariaapp.entities.EntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntryRepository extends PagingAndSortingRepository<EntryEntity, Long> {

    Optional<EntryEntity> findById(long id);

    Optional<EntryResponseDto> getById(long id);

    Optional<EntryEntity> findByReceiptNumber(Integer receiptNumber);

    List<EntryResponseDto> findAllByOrderByIdDesc();

    Page<EntryResponseDto> findAllProjectedBy(Pageable pageable);

}
