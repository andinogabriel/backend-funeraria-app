package disenodesistemas.backendfunerariaapp.dto.request;

import disenodesistemas.backendfunerariaapp.dto.DeviceInfo;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class UserLoginDto {

    @NotBlank(message = "{user.error.email.blank}")
    @Email(message = "{user.error.email.invalid }")
    String email;

    @NotBlank(message = "{user.error.password.blank}") String password;

    @NotNull(message = "{device.info.error.null}")
    DeviceInfo deviceInfo;

}
