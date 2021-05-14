package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.entities.EntryDetailEntity;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntryDetailRepository extends PagingAndSortingRepository<EntryDetailEntity, Long> {

    EntryDetailEntity findById(long id);

}
