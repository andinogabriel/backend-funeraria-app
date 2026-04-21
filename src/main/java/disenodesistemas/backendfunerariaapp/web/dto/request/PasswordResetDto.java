package disenodesistemas.backendfunerariaapp.web.dto.request;

import disenodesistemas.backendfunerariaapp.utils.ConfirmedField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@ConfirmedField(originalField = "newPassword", confirmationField = "matchingNewPassword", message = "{user.error.password.does.not.match}")
@Jacksonized
@Builder(toBuilder = true)
public record PasswordResetDto(
    @NotBlank(message = "{password.error.blank.field}") @Size(min = 8, max = 30, message = "{password.error.size.field}") String oldPassword,
    @NotBlank(message = "{password.error.blank.field}") @Size(min = 8, max = 30, message = "{password.error.size.field}") String newPassword,
    @NotBlank(message = "{password.error.blank.field}") @Size(min = 8, max = 30, message = "{password.error.size.field}") String matchingNewPassword
) {}
