package disenodesistemas.backendfunerariaapp.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class DeviceInfo {
    @NotBlank(message = "{device.info.error.device.id.blank}")
    String deviceId;
    @NotNull(message = "{device.info.error.device.type.null}")
    String deviceType;
}
