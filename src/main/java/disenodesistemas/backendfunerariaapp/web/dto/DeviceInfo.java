package disenodesistemas.backendfunerariaapp.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
public record DeviceInfo(
    @NotBlank(message = "{device.info.error.device.id.blank}") String deviceId,
    @NotNull(message = "{device.info.error.device.type.null}") String deviceType
) {}
