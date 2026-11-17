package disenodesistemas.backendfunerariaapp.web.dto.request;

import disenodesistemas.backendfunerariaapp.web.dto.DeviceInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/**
 * Body of {@code POST /api/v1/users/refresh}. The refresh token itself travels in the HttpOnly
 * {@code refresh_token} cookie, never in the body, so script running in the page cannot read it.
 */
@Jacksonized
@Builder(toBuilder = true)
public record TokenRefreshRequestDto(
    @NotNull(message = "{device.info.error.null}") DeviceInfo deviceInfo
) {}
