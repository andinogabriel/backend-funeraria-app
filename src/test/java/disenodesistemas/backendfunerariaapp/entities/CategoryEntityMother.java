package disenodesistemas.backendfunerariaapp.entities;

import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class CategoryEntityMother {
  private static final String NAME = "Coronas";
  private static final String DESCRIPTION = "Categoria de todas las coronas para sepelios";
  private static final Long ID = 1L;

  public static CategoryEntity getCategoryEntity() {
    final CategoryEntity categoryEntity = new CategoryEntity(NAME, DESCRIPTION);
    categoryEntity.setId(ID);
    categoryEntity.setItems(List.of());
    return categoryEntity;
  }
}
