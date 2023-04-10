package disenodesistemas.backendfunerariaapp.dto.request;

import disenodesistemas.backendfunerariaapp.dto.GenderDto;
import disenodesistemas.backendfunerariaapp.dto.RelationshipDto;
import disenodesistemas.backendfunerariaapp.dto.UserDto;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class DeceasedRequestDto {

    @NotBlank(message = "{deceased.error.blank.firstName}") String firstName;

    @NotBlank(message = "{deceased.deceased.blank.lastName}") String lastName;

    @NotEmpty(message = "{deceased.error.empty.dni}")
    @Positive(message = "{deceased.error.positive.dni}") Integer dni;

    @NotNull(message = "{deceased.error.empty.birthDate}")
    @Past(message = "{deceased.error.past.birthDate}") LocalDate birthDate;

    @NotNull(message = "{deceased.error.empty.deathDate}") LocalDate deathDate;

    AddressRequestDto placeOfDeath;

    @NotNull(message = "{deceased.error.empty.gender}") GenderDto gender;

    @NotNull(message = "{deceased.error.empty.relationship}") RelationshipDto userRelationship;

    @NotNull(message = "{deceased.error.empty.deathCause}") DeathCauseDto deathCause;

    UserDto user;
}
