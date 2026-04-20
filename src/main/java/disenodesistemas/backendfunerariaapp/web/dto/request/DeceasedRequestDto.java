package disenodesistemas.backendfunerariaapp.web.dto.request;

import disenodesistemas.backendfunerariaapp.web.dto.GenderDto;
import disenodesistemas.backendfunerariaapp.web.dto.RelationshipDto;
import disenodesistemas.backendfunerariaapp.web.dto.UserDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
public record DeceasedRequestDto(
    @NotBlank(message = "{deceased.error.blank.firstName}") String firstName,
    @NotBlank(message = "{deceased.deceased.blank.lastName}") String lastName,
    @NotEmpty(message = "{deceased.error.empty.dni}") @Positive(message = "{deceased.error.positive.dni}") Integer dni,
    @NotNull(message = "{deceased.error.empty.birthDate}") @Past(message = "{deceased.error.past.birthDate}") LocalDate birthDate,
    @NotNull(message = "{deceased.error.empty.deathDate}") LocalDate deathDate,
    AddressRequestDto placeOfDeath,
    @NotNull(message = "{deceased.error.empty.gender}") GenderDto gender,
    @NotNull(message = "{deceased.error.empty.relationship}") RelationshipDto deceasedRelationship,
    @NotNull(message = "{deceased.error.empty.deathCause}") DeathCauseDto deathCause,
    UserDto deceasedUser
) {}
