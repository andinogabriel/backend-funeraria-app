package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.entities.EntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntryRepository extends PagingAndSortingRepository<EntryEntity, Long> {

    EntryEntity findById(long id);

    EntryEntity findByReceiptNumber(Integer receiptNumber);

    List<EntryEntity> findAllByOrderByIdDesc();

    Page<EntryEntity> findAll(Pageable pageable);

}
