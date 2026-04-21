package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import disenodesistemas.backendfunerariaapp.web.dto.request.FuneralRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.FuneralResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface FuneralMapper {

  FuneralResponseDto toDto(Funeral entity);

  Funeral toEntity(FuneralRequestDto dto);

  void updateEntity(FuneralRequestDto dto, @MappingTarget Funeral entity);
}
