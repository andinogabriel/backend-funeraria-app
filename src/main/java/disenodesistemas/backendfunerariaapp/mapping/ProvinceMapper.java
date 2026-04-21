package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.ProvinceDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.ProvinceResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface ProvinceMapper {

  ProvinceResponseDto toDto(ProvinceEntity entity);

  ProvinceEntity toEntity(ProvinceDto dto);

  void updateEntity(ProvinceDto dto, @MappingTarget ProvinceEntity entity);
}
