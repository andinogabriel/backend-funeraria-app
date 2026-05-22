package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import disenodesistemas.backendfunerariaapp.web.dto.UserDto;

/**
 * Wire shape for an income. The two timestamps are modelled as {@link Instant} so
 * the payload always carries an unambiguous UTC moment — Jackson serialises them
 * as ISO 8601 with a trailing {@code Z} and the frontend converts to the
 * operator's local timezone on display. The previous {@code dd-MM-yyyy HH:mm}
 * pattern dropped the timezone context entirely, so the frontend rendered raw
 * "wall-clock" numbers as if they were local time when they were actually UTC.
 */
public record IncomeResponseDto(
    String receiptNumber,
    String receiptSeries,
    @JsonFormat(shape = JsonFormat.Shape.STRING) Instant incomeDate,
    @JsonFormat(shape = JsonFormat.Shape.STRING) Instant lastModifiedDate,
    BigDecimal tax,
    BigDecimal totalAmount,
    ReceiptTypeResponseDto receiptType,
    SupplierResponseDto supplier,
    UserDto incomeUser,
    UserDto lastModifiedBy,
    List<IncomeDetailResponseDto> incomeDetails
) {}
