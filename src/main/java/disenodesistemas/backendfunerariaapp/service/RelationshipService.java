package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.response.RelationshipResponseDto;
import disenodesistemas.backendfunerariaapp.entities.RelationshipEntity;

import java.util.List;

public interface RelationshipService {

    List<RelationshipResponseDto> getRelationships();

    RelationshipEntity getRelationshipById(Long id);

}
