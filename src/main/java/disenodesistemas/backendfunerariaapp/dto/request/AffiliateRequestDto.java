package disenodesistemas.backendfunerariaapp.dto.request;

import disenodesistemas.backendfunerariaapp.dto.GenderDto;
import disenodesistemas.backendfunerariaapp.dto.RelationshipDto;
import disenodesistemas.backendfunerariaapp.dto.UserDto;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class AffiliateRequestDto {

    @NotBlank(message = "{affiliate.error.firstName.blank}") String firstName;

    @NotBlank(message = "{affiliate.error.lastName.blank}") String lastName;

    @NotNull(message = "{affiliate.error.birthDate.empty}") Date birthDate;

    @NotNull(message = "{affiliate.error.dni.empty}") int dni;

    @NotNull(message = "{affiliate.error.relationship.empty}")
    @Range(min = 1, max = 31, message = "{affiliate.error.relationship.invalid}") RelationshipDto relationship;

    @NotNull(message = "{affiliate.error.gender.empty}")
    @Range(min = 1, max = 3, message = "{affiliate.error.gender.invalid}") GenderDto gender;
    @NotBlank(message = "{affiliate.error.user.empty}") UserDto user;

}
