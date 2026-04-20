package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.MobileNumberEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.MobileNumberRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MobileNumberResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface MobileNumberMapper {

  MobileNumberResponseDto toDto(MobileNumberEntity entity);

  MobileNumberEntity toEntity(MobileNumberRequestDto dto);

  void updateEntity(MobileNumberRequestDto dto, @MappingTarget MobileNumberEntity entity);
}
