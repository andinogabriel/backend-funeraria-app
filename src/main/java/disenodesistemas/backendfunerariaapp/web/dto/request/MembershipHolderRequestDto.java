package disenodesistemas.backendfunerariaapp.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/**
 * Holder ("titular") data sent with an affiliate enrolment. The DNI identifies the holder: an
 * existing holder with the same DNI is reused and its name/contact data refreshed.
 */
@Builder(toBuilder = true)
@Jacksonized
public record MembershipHolderRequestDto(
    @NotNull(message = "{holder.error.dni.empty}") @Positive(message = "{holder.error.dni.invalid}")
        Integer dni,
    @NotBlank(message = "{holder.error.firstName.blank}") @Size(max = 100) String firstName,
    @NotBlank(message = "{holder.error.lastName.blank}") @Size(max = 70) String lastName,
    @Size(max = 30) String phone,
    @Email(message = "{holder.error.email.invalid}") @Size(max = 255) String email) {}
