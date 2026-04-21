package disenodesistemas.backendfunerariaapp.web.dto.request;

import disenodesistemas.backendfunerariaapp.web.dto.DeviceInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
public record TokenRefreshRequestDto(
    @NotBlank(message = "{refresh.token.error.blank.token}") String refreshToken,
    @NotNull(message = "{device.info.error.null}") DeviceInfo deviceInfo
) {}
