package disenodesistemas.backendfunerariaapp.application.usecase.relationship;

import disenodesistemas.backendfunerariaapp.application.port.out.RelationshipPersistencePort;
import disenodesistemas.backendfunerariaapp.config.CacheConfig;
import disenodesistemas.backendfunerariaapp.domain.entity.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.RelationshipMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.RelationshipResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RelationshipQueryUseCase {

  private final RelationshipPersistencePort relationshipPersistencePort;
  private final RelationshipMapper relationshipMapper;

  @Cacheable(CacheConfig.RELATIONSHIP_CACHE)
  @Transactional(readOnly = true)
  public List<RelationshipResponseDto> getRelationships() {
    return relationshipPersistencePort.findAllByOrderByName().stream()
        .map(relationshipMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public RelationshipEntity getRelationshipById(final Long id) {
    return relationshipPersistencePort
        .findById(id)
        .orElseThrow(() -> new NotFoundException("relationship.error.not.found"));
  }
}
