package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.GenderEntity;
import disenodesistemas.backendfunerariaapp.web.dto.GenderDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.GenderResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface GenderMapper {

  GenderResponseDto toDto(GenderEntity entity);

  GenderEntity toEntity(GenderDto dto);

  void updateEntity(GenderDto dto, @MappingTarget GenderEntity entity);
}
