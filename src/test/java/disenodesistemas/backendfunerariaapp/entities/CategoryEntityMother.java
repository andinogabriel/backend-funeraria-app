package disenodesistemas.backendfunerariaapp.entities;

import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CategoryEntityMother {
  private static final String NAME = "Coronas";
  private static final String DESCRIPTION = "Categoria de todas las coronas para sepelios";
  private static final Long ID = 1L;

  public static CategoryEntity getCategoryEntity() {
    final CategoryEntity categoryEntity = new CategoryEntity(NAME, DESCRIPTION);
    categoryEntity.setId(null);
    categoryEntity.setItems(List.of());
    return categoryEntity;
  }
}
