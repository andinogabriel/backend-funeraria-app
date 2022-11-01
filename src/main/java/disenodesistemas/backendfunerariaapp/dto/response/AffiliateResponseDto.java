package disenodesistemas.backendfunerariaapp.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface AffiliateResponseDto {
    String getFirstName();
    String getLastName();
    Integer getDni();
    @JsonFormat(pattern="dd-MM-yyyy")
    LocalDate getBirthDate();
    @JsonFormat(pattern="dd-MM-yyyy")
    LocalDate getStartDate();
    GenderResponseDto getGender();
    RelationshipResponseDto getRelationship();
    UserEntity getUser();

    interface UserEntity {
        String getFirstName();
        String getLastName();
        String getEmail();
        List<MobileNumberResponseDto> getMobileNumbers();
        List<AddressResponseDto> getAddresses();
    }

}
