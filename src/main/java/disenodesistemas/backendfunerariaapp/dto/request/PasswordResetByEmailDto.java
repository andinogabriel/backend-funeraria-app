package disenodesistemas.backendfunerariaapp.dto.request;

import disenodesistemas.backendfunerariaapp.utils.ConfirmedField;
import lombok.Builder;

import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Value
@Jacksonized
@Builder(toBuilder = true)
@ConfirmedField(originalField = "password", confirmationField = "matchingPassword", message = "user.error.password.does.not.match")
public class PasswordResetByEmailDto {

    @NotBlank(message = "{password.error.blank.field}")
    @Size(min = 8, max = 30, message = "{password.error.size.field}")
    String password;

    @NotBlank(message = "{password.error.blank.field}")
    @Size(min = 8, max = 30, message = "{password.error.size.field}")
    String matchingPassword;
}
