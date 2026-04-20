package disenodesistemas.backendfunerariaapp.web.dto.request;

import disenodesistemas.backendfunerariaapp.web.dto.DeviceInfo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
public record UserLoginDto(
    @NotBlank(message = "{user.error.email.blank}") @Email(message = "{user.error.email.invalid }") String email,
    @NotBlank(message = "{user.error.password.blank}") String password,
    @NotNull(message = "{device.info.error.null}") DeviceInfo deviceInfo
) {}
