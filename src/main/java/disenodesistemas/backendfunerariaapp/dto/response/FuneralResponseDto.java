package disenodesistemas.backendfunerariaapp.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface FuneralResponseDto {
    Long getId();
    @JsonFormat(pattern="dd-MM-yyyy")
    LocalDateTime getFuneralDate();
    @JsonFormat(pattern="dd-MM-yyyy HH:mm")
    LocalDateTime getRegisterDate();
    String getReceiptNumber();
    String getReceiptSeries();
    BigDecimal getTax();
    BigDecimal getTotalAmount();
    ReceiptTypeResponseDto getReceiptType();
    DeceasedResponseDto getDeceased();
    PlanResponseDto getPlan();
}
