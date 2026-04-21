package disenodesistemas.backendfunerariaapp.web.dto.request;

import disenodesistemas.backendfunerariaapp.web.dto.DeviceInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
public record LogOutRequestDto(
    @NotNull(message = "{device.info.error.null}") DeviceInfo deviceInfo,
    @NotBlank(message = "{token.error.blank.token}") String token
) {}
