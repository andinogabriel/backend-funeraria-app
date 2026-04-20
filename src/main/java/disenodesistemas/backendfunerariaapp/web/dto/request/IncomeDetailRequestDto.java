package disenodesistemas.backendfunerariaapp.web.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
public record IncomeDetailRequestDto(
    @NotNull(message = "{incomeDetail.error.empty.quantity}") Integer quantity,
    @NotNull(message = "{incomeDetail.error.empty.purchasePrice}") @Digits(integer = 8, fraction = 2, message = "{incomeDetail.error.digits.purchasePrice}") @Positive(message = "{incomeDetail.error.positive.purchasePrice}") BigDecimal purchasePrice,
    @NotNull(message = "{incomeDetail.error.empty.salePrice}") @Digits(integer = 8, fraction = 2, message = "{incomeDetail.error.digits.salePrice}") @Positive(message = "{incomeDetail.error.positive.salePrice}") BigDecimal salePrice,
    @NotNull(message = "{incomeDetail.error.null.item}") ItemRequestDto item
) {}
