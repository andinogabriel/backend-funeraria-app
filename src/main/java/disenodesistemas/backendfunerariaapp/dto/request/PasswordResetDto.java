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
@ConfirmedField(originalField = "newPassword", confirmationField = "matchingNewPassword", message = "{user.error.password.does.not.match}")
public class PasswordResetDto {

    @NotBlank(message = "{password.error.blank.field}")
    @Size(min = 8, max = 30, message = "{password.error.size.field}")
    String oldPassword;

    @NotBlank(message = "{password.error.blank.field}")
    @Size(min = 8, max = 30, message = "{password.error.size.field}")
    String newPassword;

    @NotBlank(message = "{password.error.blank.field}")
    @Size(min = 8, max = 30, message = "{password.error.size.field}")
    String matchingNewPassword;
}
