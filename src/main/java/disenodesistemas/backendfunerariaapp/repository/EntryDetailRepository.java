package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.entities.EntryDetailEntity;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EntryDetailRepository extends PagingAndSortingRepository<EntryDetailEntity, Long> {

    Optional<EntryDetailEntity> findById(Long id);

}
