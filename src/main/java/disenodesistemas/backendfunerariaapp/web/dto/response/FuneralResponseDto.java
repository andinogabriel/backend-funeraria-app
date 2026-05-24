package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Response payload for a funeral service. {@code deletedAt} and {@code deletedBy} are
 * populated only by the admin-only papelera endpoint; the regular listing surfaces never
 * see them because every read filters {@code deletedAt is null}.
 * {@code JsonInclude.NON_NULL} keeps the two fields out of the wire shape for active
 * funerals so the existing payload stays untouched byte-for-byte.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FuneralResponseDto(
    Long id,
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm") LocalDateTime funeralDate,
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm") LocalDateTime registerDate,
    String receiptNumber,
    String receiptSeries,
    BigDecimal tax,
    BigDecimal totalAmount,
    ReceiptTypeResponseDto receiptType,
    DeceasedResponseDto deceased,
    PlanResponseDto plan,
    /** UTC instant the funeral was soft-deleted. Null for active funerals. */
    Instant deletedAt,
    /** Email of the admin that requested the soft-delete. Null for active funerals. */
    String deletedBy
) {}
