package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.CityEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.CityDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.CityResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface CityMapper {

  CityResponseDto toDto(CityEntity entity);

  CityEntity toEntity(CityDto dto);

  void updateEntity(CityDto dto, @MappingTarget CityEntity entity);
}
