package disenodesistemas.backendfunerariaapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface AffiliateResponseDto {
    String getFirstName();
    String getLastName();
    Integer getDni();
    Date getBirthDate();
    Date getStartDate();
    GenderResponseDto getGender();
    RelationshipResponseDto getRelationship();
    UserEntity getUser();

    interface UserEntity {
        String getFirstName();
        String getLastName();
        String getEmail();
        MobileNumberResponseDto getMobileNumbers();
        AddressResponseDto getAddresses();
    }

}
