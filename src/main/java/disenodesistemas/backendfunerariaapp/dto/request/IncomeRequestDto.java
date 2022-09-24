package disenodesistemas.backendfunerariaapp.dto.request;


import disenodesistemas.backendfunerariaapp.dto.ReceiptTypeDto;
import disenodesistemas.backendfunerariaapp.dto.UserDto;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class IncomeRequestDto {

    @NotNull(message = "{income.error.empty.receiptNumber}")
    @Digits(integer=20, fraction=0, message = "{income.error.digits.receiptNumber}")
    Long receiptNumber;

    @NotNull(message = "{income.error.empty.receiptSeries}")
    @Digits(integer=20, fraction=0, message = "{income.error.digits.receiptSeries}")
    Long receiptSeries;

    @NotNull(message = "{income.error.empty.tax}")
    @Digits(integer=8, fraction=2, message = "{income.error.digits.tax}")
    @DecimalMin(value = "0.00", inclusive = false, message = "{income.error.min.tax}")
    @DecimalMax(value = "100.00", message = "{income.error.max.tax}")
    BigDecimal tax;

    @NotNull(message = "{income.error.empty.receiptType}") ReceiptTypeDto receiptType;

    @NotNull(message = "{income.error.empty.incomeSupplier}") SupplierRequestDto supplier;

    UserDto incomeUser;

    List<IncomeDetailRequestDto> incomeDetails;
}