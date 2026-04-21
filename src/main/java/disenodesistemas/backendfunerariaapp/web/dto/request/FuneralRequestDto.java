package disenodesistemas.backendfunerariaapp.web.dto.request;

import disenodesistemas.backendfunerariaapp.web.dto.ReceiptTypeDto;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@Jacksonized
public record FuneralRequestDto(
    @NotNull(message = "{funeral.error.null.date}") @FutureOrPresent(message = "{funeral.error.invalid.date}") LocalDateTime funeralDate,
    String receiptNumber,
    String receiptSeries,
    @Digits(integer = 7, fraction = 2, message = "{funeral.error.digits.tax.percentage}") @Positive(message = "{funeral.error.negative.profit.percentage}") BigDecimal tax,
    ReceiptTypeDto receiptType,
    @NotNull(message = "{funeral.error.deceased.null}") DeceasedRequestDto deceased,
    @NotNull(message = "{funeral.error.plan.null}") PlanRequestDto plan
) {}
