package disenodesistemas.backendfunerariaapp.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IncomeResponseDto {

    Long getReceiptNumber();
    Long getReceiptSeries();

    @JsonFormat(pattern="dd-MM-yyyy HH:mm")
    LocalDateTime getIncomeDate();

    @JsonFormat(pattern="dd-MM-yyyy HH:mm")
    LocalDateTime getLastModifiedDate();
    BigDecimal getTax();
    BigDecimal getTotalAmount();
    ReceiptTypeResponseDto getReceiptType();
    SupplierEntity getSupplier();
    UserEntity getIncomeUser();
    UserEntity getLastModifiedBy();
    List<IncomeDetailResponseDto> getIncomeDetails();

    interface SupplierEntity {
        String getName();
        String getNif();
        String getWebPage();
        String getEmail();
    }

    interface UserEntity {
        String getFirstName();
        String getLastName();
        String getEmail();
    }

}
