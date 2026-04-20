package disenodesistemas.backendfunerariaapp.web.dto.request;

import disenodesistemas.backendfunerariaapp.web.dto.GenderDto;
import disenodesistemas.backendfunerariaapp.web.dto.RelationshipDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@Jacksonized
public record AffiliateRequestDto(
    Long id,
    @NotBlank(message = "{affiliate.error.empty.firstName}") String firstName,
    @NotBlank(message = "{affiliate.error.empty.lastName}") String lastName,
    @NotNull(message = "{affiliate.error.null.birthDate}") @Past(message = "{affiliate.error.invalid.birthDate}") LocalDate birthDate,
    @NotNull(message = "{affiliate.error.null.dni}") Integer dni,
    @NotNull(message = "{affiliate.error.null.relationship}") RelationshipDto relationship,
    @NotNull(message = "{affiliate.error.null.gender}") GenderDto gender) {}
