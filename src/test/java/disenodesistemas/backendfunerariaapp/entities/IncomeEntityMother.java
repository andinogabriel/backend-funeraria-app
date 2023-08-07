package disenodesistemas.backendfunerariaapp.entities;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.List;


@UtilityClass
public class IncomeEntityMother {

    private static final Long RECEIPT_NUMBER = 1L;
    private static final Long RECEIPT_SERIES = 45555L;
    private static final BigDecimal TAX = BigDecimal.TEN;

    public static IncomeEntity getIncome() {
        final IncomeEntity income = new IncomeEntity(
                RECEIPT_NUMBER, RECEIPT_SERIES, TAX, ReceiptTypeEntityMother.getReceipt(),
                SupplierEntityMother.getSupplier(), UserEntityMother.getUser()
        );
        income.setId(1L);
        income.setIncomeDetails(List.of(IncomeDetailEntityMother.getIncomeDetail()));
        income.setTotalAmount(BigDecimal.valueOf(123));
        return income;
    }

}