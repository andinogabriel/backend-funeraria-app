package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.BrandEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.BrandRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.BrandResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface BrandMapper {

  BrandResponseDto toDto(BrandEntity entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "brandItems", ignore = true)
  BrandEntity toEntity(BrandRequestDto dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "brandItems", ignore = true)
  void updateEntity(BrandRequestDto dto, @MappingTarget BrandEntity entity);
}
