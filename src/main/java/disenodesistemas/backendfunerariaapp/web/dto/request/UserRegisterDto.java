package disenodesistemas.backendfunerariaapp.web.dto.request;

import disenodesistemas.backendfunerariaapp.web.dto.validation.PasswordMatches;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@PasswordMatches
@Jacksonized
@Builder(toBuilder = true)
public record UserRegisterDto(
    @NotBlank(message = "{user.error.lastName.blank}") String lastName,
    @NotBlank(message = "{user.error.firstName.blank}") String firstName,
    @NotBlank(message = "{user.error.email.blank}") @Email(message = "{user.error.email.invalid}") String email,
    @NotBlank(message = "{user.error.password.blank}") @Size(min = 8, max = 30, message = "{user.error.password.size}") String password,
    @NotBlank(message = "{user.error.password.blank}") @Size(min = 8, max = 30, message = "{user.error.password.size}") String matchingPassword,
    Set<String> roles,
    List<MobileNumberRequestDto> mobileNumbers,
    List<AddressRequestDto> addresses
) {}
