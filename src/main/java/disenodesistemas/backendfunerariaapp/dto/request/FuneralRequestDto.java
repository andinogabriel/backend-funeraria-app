package disenodesistemas.backendfunerariaapp.dto.request;

import disenodesistemas.backendfunerariaapp.dto.ReceiptTypeDto;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.Digits;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class FuneralRequestDto {

    @NotNull(message = "{funeral.error.null.date}")
    @FutureOrPresent(message = "{funeral.error.invalid.date}")
    LocalDateTime funeralDate;

    @NotBlank(message = "{funeral.error.receiptNumber.blank}") String receiptNumber;
    @NotBlank(message = "{funeral.error.receiptSeries.blank}") String receiptSeries;

    @Digits(integer=7, fraction=2, message = "{funeral.error.digits.tax.percentage}")
    @Positive(message = "{funeral.error.negative.profit.percentage}")
    BigDecimal tax;

    @NotNull(message = "{funeral.error.receiptType.null}")
    ReceiptTypeDto receiptType;

    @NotNull(message = "{funeral.error.deceased.null}")
    DeceasedRequestDto deceased;

    @NotNull(message = "{funeral.error.plan.null}")
    PlanRequestDto plan;

}
