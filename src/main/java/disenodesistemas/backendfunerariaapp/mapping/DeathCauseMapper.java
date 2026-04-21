package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.DeathCauseEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.DeathCauseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DeathCauseResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface DeathCauseMapper {

  DeathCauseResponseDto toDto(DeathCauseEntity entity);

  DeathCauseEntity toEntity(DeathCauseDto dto);

  void updateEntity(DeathCauseDto dto, @MappingTarget DeathCauseEntity entity);
}
