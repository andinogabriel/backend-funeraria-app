package disenodesistemas.backendfunerariaapp.dto.request;

import disenodesistemas.backendfunerariaapp.dto.GenderDto;
import disenodesistemas.backendfunerariaapp.dto.RelationshipDto;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.LocalDate;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class AffiliateRequestDto {

    Long id;
    @NotBlank(message = "{affiliate.error.firstName.blank}") String firstName;

    @NotBlank(message = "{affiliate.error.lastName.blank}") String lastName;

    @Past(message = "affiliate.error.birthDate.past")
    @NotNull(message = "{affiliate.error.birthDate.empty}") LocalDate birthDate;

    @NotNull(message = "{affiliate.error.dni.empty}") Integer dni;

    @NotNull(message = "{affiliate.error.relationship.empty}") RelationshipDto relationship;

    @NotNull(message = "{affiliate.error.gender.empty}") GenderDto gender;
}
