package disenodesistemas.backendfunerariaapp.dto.request;

import disenodesistemas.backendfunerariaapp.dto.DeviceInfo;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class LogOutRequestDto {
    @NotNull(message = "{device.info.error.null}")
    DeviceInfo deviceInfo;
    @NotBlank(message = "{token.error.blank.token}")
    String token;
}
