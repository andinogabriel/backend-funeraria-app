package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import disenodesistemas.backendfunerariaapp.web.dto.UserDto;

public record IncomeResponseDto(
    String receiptNumber,
    String receiptSeries,
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm") LocalDateTime incomeDate,
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm") LocalDateTime lastModifiedDate,
    BigDecimal tax,
    BigDecimal totalAmount,
    ReceiptTypeResponseDto receiptType,
    SupplierResponseDto supplier,
    UserDto incomeUser,
    UserDto lastModifiedBy,
    List<IncomeDetailResponseDto> incomeDetails
) {}