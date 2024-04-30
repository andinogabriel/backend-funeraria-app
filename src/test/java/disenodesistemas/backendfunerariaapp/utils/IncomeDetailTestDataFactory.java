package disenodesistemas.backendfunerariaapp.utils;

import disenodesistemas.backendfunerariaapp.dto.request.IncomeDetailRequestDto;
import disenodesistemas.backendfunerariaapp.entities.IncomeDetailEntity;

import java.math.BigDecimal;
import java.util.List;

import lombok.experimental.UtilityClass;

import static disenodesistemas.backendfunerariaapp.utils.ItemTestDataFactory.getItemRequest;

@UtilityClass
public class IncomeDetailTestDataFactory {

  private static final Integer QUANTITY = 3;
  private static final BigDecimal PURCHASE_PRICE = BigDecimal.valueOf(500);
  private static final BigDecimal SALE_PRICE = BigDecimal.valueOf(1000);

  public static IncomeDetailEntity getIncomeDetail() {
    final IncomeDetailEntity incomeDetailEntity = new IncomeDetailEntity();
    incomeDetailEntity.setId(1L);
    incomeDetailEntity.setItem(ItemTestDataFactory.getItem());
    incomeDetailEntity.setPurchasePrice(PURCHASE_PRICE);
    incomeDetailEntity.setSalePrice(SALE_PRICE);
    incomeDetailEntity.setQuantity(QUANTITY);
    incomeDetailEntity.setId(1L);
    return incomeDetailEntity;
  }

  public static IncomeDetailRequestDto getIncomeDetailDto() {
    return IncomeDetailRequestDto.builder()
        .item(getItemRequest())
        .purchasePrice(PURCHASE_PRICE)
        .quantity(QUANTITY)
        .salePrice(SALE_PRICE)
        .build();
  }

  public static List<IncomeDetailRequestDto> getIncomeDetails() {
    return List.of(
        IncomeDetailRequestDto.builder()
            .item(getItemRequest())
            .purchasePrice(PURCHASE_PRICE)
            .quantity(QUANTITY)
            .salePrice(SALE_PRICE)
            .build());
  }
}
