package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import disenodesistemas.backendfunerariaapp.domain.enums.IncomeStatus;
import disenodesistemas.backendfunerariaapp.web.dto.UserDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Wire shape for an income. The two timestamps are modelled as {@link Instant} so
 * the payload always carries an unambiguous UTC moment — Jackson serialises them
 * as ISO 8601 with a trailing {@code Z} and the frontend converts to the
 * operator's local timezone on display.
 *
 * <p>The {@code id} field is exposed because the annul endpoint takes the primary
 * key (not the receipt number) as its path variable, and the operator UI needs to
 * hand that id back to the server when wiring the "Anular" action.
 */
public record IncomeResponseDto(
    Long id,
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
    List<IncomeDetailResponseDto> incomeDetails,
    /** Lifecycle state of the receipt. */
    IncomeStatus status,
    /**
     * When the row IS a reversal counter-entry, the id of the original receipt this
     * cancels. {@code null} on every {@code ACTIVE} non-reversal entry and on every
     * {@code ANNULLED} original. The frontend uses it to render the
     * "Reversion de #N" badge linkage.
     */
    Long reversalOfId
) {}
