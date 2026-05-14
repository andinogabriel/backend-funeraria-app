package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.ItemRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.ItemResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface ItemMapper {

  ItemResponseDto toDto(ItemEntity entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "code", ignore = true)
  @Mapping(target = "itemImageLink", ignore = true)
  @Mapping(target = "stock", ignore = true)
  @Mapping(target = "itemsPlan", ignore = true)
  @Mapping(target = "incomeDetails", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  ItemEntity toEntity(ItemRequestDto dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "code", ignore = true)
  @Mapping(target = "itemImageLink", ignore = true)
  @Mapping(target = "stock", ignore = true)
  @Mapping(target = "itemsPlan", ignore = true)
  @Mapping(target = "incomeDetails", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  void updateEntity(ItemRequestDto dto, @MappingTarget ItemEntity entity);
}
