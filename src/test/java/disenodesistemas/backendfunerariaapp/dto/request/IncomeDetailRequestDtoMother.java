package disenodesistemas.backendfunerariaapp.dto.request;

import java.math.BigDecimal;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IncomeDetailRequestDtoMother {

  private static final Integer QUANTITY = 3;
  private static final BigDecimal PURCHASE_PRICE = BigDecimal.valueOf(500);
  private static final BigDecimal SALE_PRICE = BigDecimal.valueOf(1000);

  public static IncomeDetailRequestDto getIncomeDetail() {
    return IncomeDetailRequestDto.builder()
        .item(ItemMother.getItemRequest())
        .purchasePrice(PURCHASE_PRICE)
        .quantity(QUANTITY)
        .salePrice(SALE_PRICE)
        .build();
  }

  public static List<IncomeDetailRequestDto> getIncomeDetails() {
    return List.of(
        IncomeDetailRequestDto.builder()
            .item(ItemMother.getItemRequest())
            .purchasePrice(PURCHASE_PRICE)
            .quantity(QUANTITY)
            .salePrice(SALE_PRICE)
            .build());
  }
}
