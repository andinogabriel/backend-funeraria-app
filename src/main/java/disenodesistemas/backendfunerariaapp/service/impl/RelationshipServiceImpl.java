package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.response.RelationshipResponseDto;
import disenodesistemas.backendfunerariaapp.entities.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.RelationshipRepository;
import disenodesistemas.backendfunerariaapp.service.RelationshipService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RelationshipServiceImpl implements RelationshipService {

  private final RelationshipRepository relationshipRepository;

  @Override
  public List<RelationshipResponseDto> getRelationships() {
    return relationshipRepository.findAllByOrderByName();
  }

  @Override
  public RelationshipEntity getRelationshipById(final Long id) {
    return relationshipRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("relationship.error.not.found"));
  }
}
