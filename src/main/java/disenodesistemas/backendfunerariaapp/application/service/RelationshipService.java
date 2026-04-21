package disenodesistemas.backendfunerariaapp.application.service;

import disenodesistemas.backendfunerariaapp.web.dto.response.RelationshipResponseDto;
import disenodesistemas.backendfunerariaapp.domain.entity.RelationshipEntity;

import java.util.List;

public interface RelationshipService {

    List<RelationshipResponseDto> getRelationships();

    RelationshipEntity getRelationshipById(Long id);

}
