package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.response.RelationshipResponseDto;
import disenodesistemas.backendfunerariaapp.entities.RelationshipEntity;

import java.util.List;

public interface IRelationship {

    List<RelationshipResponseDto> getRelationships();

    RelationshipEntity getRelationshipById(long id);

}
