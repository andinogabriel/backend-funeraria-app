package disenodesistemas.backendfunerariaapp.utils;

import disenodesistemas.backendfunerariaapp.dto.request.BrandRequestDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class BrandTestDataFactory {

  private static final String NAME = "Marca primer nivel";
  private static final String WEB_PAGE = "www.marcaprimernivel.com";

  public static BrandRequestDto getBrandRequestDto() {
    return BrandRequestDto.builder().id(1L).name(NAME).webPage(WEB_PAGE).build();
  }

  public static BrandEntity getBrandEntity() {
    return BrandEntity.builder().id(1L).name(NAME).webPage(WEB_PAGE).build();
  }

  public static BrandEntity getBrandEntityIdNull() {
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
