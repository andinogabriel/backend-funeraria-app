package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.web.dto.RelationshipDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.RelationshipResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface RelationshipMapper {

  RelationshipResponseDto toDto(RelationshipEntity entity);

  RelationshipEntity toEntity(RelationshipDto dto);

  void updateEntity(RelationshipDto dto, @MappingTarget RelationshipEntity entity);
}
