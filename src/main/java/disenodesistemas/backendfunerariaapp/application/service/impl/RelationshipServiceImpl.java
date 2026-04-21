package disenodesistemas.backendfunerariaapp.application.service.impl;

import disenodesistemas.backendfunerariaapp.application.service.RelationshipService;
import disenodesistemas.backendfunerariaapp.application.usecase.relationship.RelationshipQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.web.dto.response.RelationshipResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RelationshipServiceImpl implements RelationshipService {
  private final RelationshipQueryUseCase relationshipQueryUseCase;

  @Override
  public List<RelationshipResponseDto> getRelationships() {
    return relationshipQueryUseCase.getRelationships();
  }

  @Override
  public RelationshipEntity getRelationshipById(final Long id) {
    return relationshipQueryUseCase.getRelationshipById(id);
  }
}
