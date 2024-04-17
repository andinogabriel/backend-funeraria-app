package disenodesistemas.backendfunerariaapp.dto.request;

import static disenodesistemas.backendfunerariaapp.entities.BrandEntityMother.getBrandEntity;
import static disenodesistemas.backendfunerariaapp.entities.CategoryEntityMother.getCategoryEntity;

import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import java.math.BigDecimal;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ItemMother {

  private static final String NAME = "Corona simple";
  private static final String CODE = "itemCode";
  private static final BigDecimal PRICE = BigDecimal.valueOf(3000);

  public static ItemRequestDto getItemRequest() {
    return ItemRequestDto.builder()
        .id(1L)
        .brand(BrandRequestDto.builder().id(1L).name("Marcaza").build())
        .category(CategoryRequestDto.builder().id(1L).name("Coronas").build())
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
