package disenodesistemas.backendfunerariaapp.dto;

import disenodesistemas.backendfunerariaapp.dto.request.BrandRequestDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BrandDtoMother {

  private static final String NAME = "Marca primer nivel";
  private static final String WEB_PAGE = "www.brandpage.com";

  public static BrandRequestDto getBrandRequestDto() {
    return BrandRequestDto.builder().id(1L).name(NAME).webPage(WEB_PAGE).build();
  }

  public static BrandEntity getBrandEntity() {
    return BrandEntity.builder().id(1L).name(NAME).webPage(WEB_PAGE).build();
  }
}
