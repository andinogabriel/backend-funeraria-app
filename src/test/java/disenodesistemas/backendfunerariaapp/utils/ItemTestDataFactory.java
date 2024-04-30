package disenodesistemas.backendfunerariaapp.utils;

import disenodesistemas.backendfunerariaapp.dto.request.ItemRequestDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import java.math.BigDecimal;
import lombok.experimental.UtilityClass;

import static disenodesistemas.backendfunerariaapp.utils.BrandTestDataFactory.getBrandRequestDto;
import static disenodesistemas.backendfunerariaapp.utils.CategoryTestDataFactory.getCategoryRequestDto;

@UtilityClass
public class ItemTestDataFactory {

  private static final String NAME = "Corona simple";
  private static final String CODE = "itemCode";
  private static final BigDecimal PRICE = BigDecimal.valueOf(3000);

  public static ItemEntity getItem() {
    final BrandEntity brand = new BrandEntity();
    brand.setId(1L);
    brand.setName("Marcaza");
    final CategoryEntity category = new CategoryEntity();
    category.setId(1L);
    category.setName("Coronas");
    final ItemEntity itemEntity =
        new ItemEntity(NAME, null, CODE, PRICE, null, null, null, category, brand);
    itemEntity.setId(1L);
    return itemEntity;
  }

  public static ItemRequestDto getItemRequest() {
    return ItemRequestDto.builder()
        .id(1L)
        .brand(getBrandRequestDto())
        .category(getCategoryRequestDto())
        .code("67ad6c26-f586-4cb2-9d5e-3fbcc3e2e8eb")
        .price(PRICE)
        .name(NAME)
        .build();
  }
}
