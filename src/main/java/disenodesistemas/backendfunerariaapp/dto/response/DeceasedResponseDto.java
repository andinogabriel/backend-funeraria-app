package disenodesistemas.backendfunerariaapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface DeceasedResponseDto {

    long getId();
    String getFirstName();
    String getLastName();
    Integer getDni();
    Date getBirthDate();
    Date getRegisterDate();
    Date getDeathDate();
    AddressResponseDto getPlaceOfDeath();
    RelationshipResponseDto getDeceasedRelationship();
    UserEntity getDeceasedUser();
    GenderResponseDto getDeceasedGender();
    DeathCauseResponseDto getDeceasedDeathCause();

    interface UserEntity {
        long getId();
        String getFirstName();
        String getLastName();
        String getEmail();
    }


}
