package disenodesistemas.backendfunerariaapp.web.dto.request;

import disenodesistemas.backendfunerariaapp.web.dto.ReceiptTypeDto;
import disenodesistemas.backendfunerariaapp.web.dto.UserDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@Jacksonized
public record IncomeRequestDto(
    Long receiptNumber,
    Long receiptSeries,
    @NotNull(message = "{income.error.null.tax}") @DecimalMin(value = "0.0", message = "{income.error.invalid.tax}") BigDecimal tax,
    UserDto incomeUser,
    ReceiptTypeDto receiptType,
    SupplierRequestDto supplier,
    @NotNull(message = "{income.error.null.incomeDetails}") @NotEmpty(message = "{income.error.empty.incomeDetails}")
    List<@NotNull(message = "{income.error.null.incomeDetail}") @Valid IncomeDetailRequestDto> incomeDetails
) {}
