package disenodesistemas.backendfunerariaapp.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface DeceasedResponseDto {

  Long getId();

  String getFirstName();

  String getLastName();

  Integer getDni();

  @JsonFormat(pattern = "dd-MM-yyyy")
  LocalDate getBirthDate();

  @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
  LocalDateTime getRegisterDate();

  @JsonFormat(pattern = "dd-MM-yyyy")
  LocalDate getDeathDate();

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
