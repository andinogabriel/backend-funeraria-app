package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.entities.RelationshipEntity;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelationshipRepository extends PagingAndSortingRepository<RelationshipEntity, Long> {

    RelationshipEntity findById(long id);
}
