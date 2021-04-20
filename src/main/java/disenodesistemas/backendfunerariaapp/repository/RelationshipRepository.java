package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.entities.RelationshipEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelationshipRepository extends PagingAndSortingRepository<RelationshipEntity, Long> {

    RelationshipEntity findById(long id);

    List<RelationshipEntity> findAll();
}
