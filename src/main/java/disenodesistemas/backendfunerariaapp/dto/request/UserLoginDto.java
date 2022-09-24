package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class UserLoginDto {

    @NotBlank(message = "{user.error.email.blank}")
    @Email(message = "{user.error.email.invalid }")
    String email;

    @NotBlank(message = "{user.error.password.blank}") String password;

}
