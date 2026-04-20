package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DeceasedResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface DeceasedMapper {

  DeceasedResponseDto toDto(DeceasedEntity entity);

  DeceasedEntity toEntity(DeceasedRequestDto dto);

  void updateEntity(DeceasedRequestDto dto, @MappingTarget DeceasedEntity entity);
}
