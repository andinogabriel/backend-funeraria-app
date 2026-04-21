package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    PlanResponseDto plan
) {}