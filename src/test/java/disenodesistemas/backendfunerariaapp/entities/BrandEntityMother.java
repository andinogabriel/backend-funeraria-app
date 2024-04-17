package disenodesistemas.backendfunerariaapp.entities;

import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BrandEntityMother {

  private static final String NAME = "Marca primer nivel";
  private static final String WEB_PAGE = "www.brandpage.com";

  public static BrandEntity getBrandEntity() {
    return BrandEntity.builder()
        .brandItems(List.of())
        .webPage(WEB_PAGE)
        .id(null)
        .name(NAME)
        .build();
  }

  public static BrandEntity getBrandEntityWithItems() {
    return BrandEntity.builder()
        .brandItems(List.of(new ItemEntity()))
        .webPage(WEB_PAGE)
        .id(1L)
        .name(NAME)
        .build();
  }
}
