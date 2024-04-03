package disenodesistemas.backendfunerariaapp.entities;

import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class BrandEntityMother {

  private static final String NAME = "Marca primer nivel";
  private static final String WEB_PAGE = "www.brandpage.com";

  public static BrandEntity getBrandEntity() {
    return BrandEntity.builder().brandItems(List.of()).webPage(WEB_PAGE).id(1L).name(NAME).build();
  }
}
