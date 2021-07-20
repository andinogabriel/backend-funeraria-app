package disenodesistemas.backendfunerariaapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface EntryResponseDto {

    long getId();
    Integer getReceiptNumber();
    Integer getReceiptSeries();
    Date getEntryDate();
    LocalDateTime getLastModifiedDate();
    BigDecimal getTax();
    BigDecimal getTotalAmount();
    ReceiptTypeResponseDto getReceiptType();
    SupplierEntity getEntrySupplier();
    UserEntity getEntryUser();
    UserEntity getLastModifiedBy();
    List<EntryDetailResponseDto> getEntryDetails();

    interface SupplierEntity {
        long getId();
        String getName();
        String getNif();
        String getWebPage();
        String getEmail();
    }

    interface UserEntity {
        long getId();
        String getFirstName();
        String getLastName();
        String getEmail();
    }

}
