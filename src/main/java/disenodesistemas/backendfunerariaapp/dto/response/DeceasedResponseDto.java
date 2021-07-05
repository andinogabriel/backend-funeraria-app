package disenodesistemas.backendfunerariaapp.dto.response;

import disenodesistemas.backendfunerariaapp.entities.UserEntity;

import java.util.Date;

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
