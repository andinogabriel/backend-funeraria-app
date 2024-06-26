package disenodesistemas.backendfunerariaapp.utils;

import disenodesistemas.backendfunerariaapp.dto.request.CategoryRequestDto;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CategoryTestDataFactory {

  private static final String NAME = "Coronas";
  private static final String DESCRIPTION = "Categoria de todas las coronas para sepelios";
  private static final Long ID = 1L;

  public static CategoryRequestDto getCategoryRequestDto() {
    return CategoryRequestDto.builder().id(ID).name(NAME).description(DESCRIPTION).build();
  }

  public static CategoryEntity getCategoryEntity() {
    final CategoryEntity categoryEntity = new CategoryEntity(NAME, DESCRIPTION);
    categoryEntity.setId(null);
    categoryEntity.setItems(List.of());
    return categoryEntity;
  }

  public static CategoryEntity getCategoryEntityWithId() {
    final CategoryEntity categoryEntity = new CategoryEntity(NAME, DESCRIPTION);
    categoryEntity.setId(ID);
    categoryEntity.setItems(List.of());
    return categoryEntity;
  }

  public static CategoryEntity getCategoryEntityWithItems() {
    final CategoryEntity categoryEntity = new CategoryEntity(NAME, DESCRIPTION);
    categoryEntity.setId(null);
    categoryEntity.setItems(List.of(new ItemEntity()));
    return categoryEntity;
  }
}
