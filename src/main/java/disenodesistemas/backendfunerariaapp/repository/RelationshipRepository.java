package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.dto.response.RelationshipResponseDto;
import disenodesistemas.backendfunerariaapp.entities.RelationshipEntity;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RelationshipRepository extends PagingAndSortingRepository<RelationshipEntity, Long> {

    Optional<RelationshipEntity> findById(Long id);

    List<RelationshipResponseDto> findAllByOrderByName();
}
