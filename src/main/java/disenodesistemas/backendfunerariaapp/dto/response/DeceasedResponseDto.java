package disenodesistemas.backendfunerariaapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface DeceasedResponseDto {

    long getId();
    String getFirstName();
    String getLastName();
    Integer getDni();
    LocalDate getBirthDate();
    LocalDateTime getRegisterDate();
    LocalDateTime getDeathDate();
    AddressResponseDto getPlaceOfDeath();
    RelationshipResponseDto getDeceasedRelationship();
    UserEntity getDeceasedUser();
    GenderResponseDto getGender();
    DeathCauseResponseDto getDeathCause();

    interface UserEntity {
        String getFirstName();
        String getLastName();
        String getEmail();
    }


}
