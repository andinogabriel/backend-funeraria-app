package disenodesistemas.backendfunerariaapp.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import disenodesistemas.backendfunerariaapp.security.PasswordMatches;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Set;

@PasswordMatches
@Value
@Jacksonized
@Builder(toBuilder = true)
public class UserRegisterDto {

    @NotBlank(message = "{user.error.lastName.blank}") String lastName;

    @NotBlank(message = "{user.error.firstName.blank}") String firstName;

    @NotBlank(message = "{user.error.email.blank}")
    @Email(message = "{user.error.email.invalid}")
    String email;

    @NotBlank(message = "{user.error.password.blank}")
    @Size(min = 8, max = 30, message = "{user.error.password.size}") //Size es para strings
    String password;

    @NotBlank(message = "{user.error.password.blank}")
    @Size(min = 8, max = 30, message = "{user.error.password.size}")
    String matchingPassword;

    Set<String> roles;
}
