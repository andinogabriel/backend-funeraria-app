package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.RelationshipPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.persistence.repository.RelationshipRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaRelationshipPersistenceAdapter implements RelationshipPersistencePort {

  private final RelationshipRepository relationshipRepository;

  @Override
  public Optional<RelationshipEntity> findById(final Long id) {
    return relationshipRepository.findById(id);
  }

  @Override
  public List<RelationshipEntity> findAllByOrderByName() {
    return relationshipRepository.findAllByOrderByName();
  }
}
