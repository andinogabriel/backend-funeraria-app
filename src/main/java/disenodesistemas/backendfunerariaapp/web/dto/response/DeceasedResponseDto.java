package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import disenodesistemas.backendfunerariaapp.web.dto.UserDto;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DeceasedResponseDto(
    Long id,
    String firstName,
    String lastName,
    Integer dni,
    boolean affiliated,
    @JsonFormat(pattern = "dd-MM-yyyy") LocalDate birthDate,
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm") LocalDateTime registerDate,
    @JsonFormat(pattern = "dd-MM-yyyy") LocalDate deathDate,
    AddressResponseDto placeOfDeath,
    RelationshipResponseDto deceasedRelationship,
    UserDto deceasedUser,
    GenderResponseDto gender,
    DeathCauseResponseDto deathCause
) {}