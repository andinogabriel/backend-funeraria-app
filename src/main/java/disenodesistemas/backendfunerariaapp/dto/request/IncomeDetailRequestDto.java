package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class IncomeDetailRequestDto {

    @NotNull(message = "{incomeDetail.error.empty.quantity}") Integer quantity;

    @NotNull(message = "{incomeDetail.error.empty.purchasePrice}")
    @Digits(integer=8, fraction=2, message = "{incomeDetail.error.digits.purchasePrice}")
    @Positive(message = "{incomeDetail.error.positive.purchasePrice}") BigDecimal purchasePrice;

    @NotNull(message = "{incomeDetail.error.empty.salePrice}")
    @Digits(integer=8, fraction=2, message = "{incomeDetail.error.digits.salePrice}")
    @Positive(message = "{incomeDetail.error.positive.salePrice}") BigDecimal salePrice;

    @NotNull(message = "{incomeDetail.error.null.item}") ItemRequestDto item;
}
