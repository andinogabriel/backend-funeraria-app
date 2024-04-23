package disenodesistemas.backendfunerariaapp.dto.request;

import static disenodesistemas.backendfunerariaapp.dto.BrandDtoMother.getBrandRequestDto;
import static disenodesistemas.backendfunerariaapp.dto.CategoryDtoMother.getCategoryRequestDto;
import static disenodesistemas.backendfunerariaapp.entities.BrandEntityMother.getBrandEntity;
import static disenodesistemas.backendfunerariaapp.entities.CategoryEntityMother.getCategoryEntity;

import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import java.math.BigDecimal;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ItemMother {

  private static final String NAME = "Corona simple";
  private static final String CODE = "67ad6c26-f586-4cb2-9d5e-3fbcc3e2e8eb";
  private static final BigDecimal PRICE = BigDecimal.valueOf(3000);

  public static ItemRequestDto getItemRequest() {
    return ItemRequestDto.builder()
        .id(1L)
        .brand(getBrandRequestDto())
        .category(getCategoryRequestDto())
        .code(CODE)
        .price(PRICE)
        .name(NAME)
        .build();
  }

  public static ItemEntity getItemEntity() {
    return ItemEntity.builder()
        .id(1L)
        .code(CODE)
        .price(PRICE)
        .name(NAME)
        .brand(getBrandEntity())
        .category(getCategoryEntity())
        .stock(0)
        .build();
  }

  public static ItemEntity getAnotherItemEntity() {
    return ItemEntity.builder()
        .id(null)
        .code("anotherItemCode")
        .price(BigDecimal.TEN.add(PRICE))
        .name("Corona de flores")
        .brand(getBrandEntity())
        .category(getCategoryEntity())
        .stock(1)
        .build();
  }

  public static ItemRequestPlanDto getItemRequestPlanDto() {
    return ItemRequestPlanDto.builder().build();
  }
}
