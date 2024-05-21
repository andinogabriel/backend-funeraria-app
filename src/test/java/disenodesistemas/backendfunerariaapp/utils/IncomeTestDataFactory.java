package disenodesistemas.backendfunerariaapp.utils;

import static disenodesistemas.backendfunerariaapp.utils.IncomeDetailTestDataFactory.getIncomeDetailDto;
import static disenodesistemas.backendfunerariaapp.utils.ReceiptTypeTestDataFactory.getIncomeCashReceipt;
import static disenodesistemas.backendfunerariaapp.utils.SupplierTestDataFactory.getSupplierRequestDto;
import static disenodesistemas.backendfunerariaapp.utils.UserTestDataFactory.getUserDto;

import disenodesistemas.backendfunerariaapp.dto.request.IncomeRequestDto;
import disenodesistemas.backendfunerariaapp.entities.IncomeEntity;
import java.math.BigDecimal;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IncomeTestDataFactory {

  private static final Long RECEIPT_NUMBER = 1L;
  private static final Long RECEIPT_SERIES = 45555L;
  private static final BigDecimal TAX = BigDecimal.TEN;

  public static IncomeEntity getIncome() {
    final IncomeEntity income =
        new IncomeEntity(
            RECEIPT_NUMBER,
            RECEIPT_SERIES,
            TAX,
            ReceiptTypeTestDataFactory.getCashReceipt(),
            SupplierTestDataFactory.getSupplierEntity(),
            UserTestDataFactory.getUserEntity());
    income.setId(1L);
    income.setIncomeDetails(List.of(IncomeDetailTestDataFactory.getIncomeDetail()));
    income.setTotalAmount(BigDecimal.valueOf(123));
    return income;
  }

  public static IncomeRequestDto getIncomeRequest() {
    return IncomeRequestDto.builder()
        .tax(TAX)
        .receiptType(getIncomeCashReceipt())
        .supplier(getSupplierRequestDto())
        .incomeUser(getUserDto())
        .incomeDetails(List.of(getIncomeDetailDto()))
        .build();
  }
}
