package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.SupplierEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.SupplierRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.SupplierResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface SupplierMapper {

  SupplierResponseDto toDto(SupplierEntity entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "incomes", ignore = true)
  SupplierEntity toEntity(SupplierRequestDto dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "incomes", ignore = true)
  void updateEntity(SupplierRequestDto dto, @MappingTarget SupplierEntity entity);
}
