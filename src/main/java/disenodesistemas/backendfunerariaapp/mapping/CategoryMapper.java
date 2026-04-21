package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.CategoryRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.CategoryResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = MapStructConfig.class)
public interface CategoryMapper {

  CategoryResponseDto toDto(CategoryEntity entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "items", ignore = true)
  @Mapping(target = "name", source = "name", qualifiedByName = "capitalize")
  CategoryEntity toEntity(CategoryRequestDto dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "items", ignore = true)
  @Mapping(target = "name", source = "name", qualifiedByName = "capitalize")
  void updateEntity(CategoryRequestDto dto, @MappingTarget CategoryEntity entity);

  @Named("capitalize")
  default String capitalize(final String value) {
    if (value == null || value.isBlank()) {
      return value;
    }
    return value.substring(0, 1).toUpperCase() + value.substring(1);
  }
}
