package disenodesistemas.backendfunerariaapp.dto.request;

import static disenodesistemas.backendfunerariaapp.dto.ReceiptTypeDtoMother.getReciboDeCaja;
import static disenodesistemas.backendfunerariaapp.dto.UserDtoMother.getUserDto;
import static disenodesistemas.backendfunerariaapp.dto.request.IncomeDetailRequestDtoMother.getIncomeDetail;
import static disenodesistemas.backendfunerariaapp.dto.request.SupplierRequestDtoMother.getSupplier;

import java.math.BigDecimal;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IncomeRequestDtoMother {

  public static IncomeRequestDto getIncomeRequest() {
    return IncomeRequestDto.builder()
        .tax(BigDecimal.TEN)
        .receiptType(getReciboDeCaja())
        .supplier(getSupplier())
        .incomeUser(getUserDto())
        .incomeDetails(List.of(getIncomeDetail()))
        .build();
  }
}
