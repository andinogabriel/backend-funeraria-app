package disenodesistemas.backendfunerariaapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface AffiliateResponseDto {

    long getId();
    String getFirstName();
    String getLastName();
    Integer getDni();
    Date getBirthDate();
    Date getStartDate();
    GenderResponseDto getAffiliateGender();
    RelationshipResponseDto getAffiliateRelationship();
    UserEntity getUser();

    interface UserEntity {
        long getId();
        String getFirstName();
        String getLastName();
        String getEmail();
        MobileNumberResponseDto getMobileNumbers();
        AddressResponseDto getAddresses();
    }

}
