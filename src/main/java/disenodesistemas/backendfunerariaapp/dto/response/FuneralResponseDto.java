package disenodesistemas.backendfunerariaapp.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface FuneralResponseDto {
    Long getId();
    LocalDateTime getFuneralDate();
    LocalDateTime getRegisterDate();
    String getReceiptNumber();
    String getReceiptSeries();
    BigDecimal getTax();
    BigDecimal getTotalAmount();
    ReceiptTypeResponseDto getReceiptType();
    DeceasedResponseDto getDeceased();
    PlanResponseDto getPlan();
}
