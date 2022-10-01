package disenodesistemas.backendfunerariaapp.entities;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;


@UtilityClass
public class IncomeDetailEntityMother {

    private static final Integer QUANTITY = 3;
    private static final BigDecimal PURCHASE_PRICE = BigDecimal.valueOf(500);
    private static final BigDecimal SALE_PRICE = BigDecimal.valueOf(1000);


    public static IncomeDetailEntity getIncomeDetail() {
        final IncomeDetailEntity incomeDetailEntity = new IncomeDetailEntity();
        incomeDetailEntity.setId(1L);
        incomeDetailEntity.setItem(ItemEntityMother.getItem());
        incomeDetailEntity.setPurchasePrice(PURCHASE_PRICE);
        incomeDetailEntity.setSalePrice(SALE_PRICE);
        incomeDetailEntity.setQuantity(QUANTITY);
        incomeDetailEntity.setId(1L);
        return incomeDetailEntity;
    }

}