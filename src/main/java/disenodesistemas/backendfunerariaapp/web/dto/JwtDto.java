package disenodesistemas.backendfunerariaapp.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/**
 * Session issued by login and refresh. {@code refreshToken} is carried to the controller, which
 * moves it into the HttpOnly {@code refresh_token} cookie; it is never written to the JSON body,
 * so page script (and anything injected into it) cannot read it.
 */
@Builder(toBuilder = true)
@Jacksonized
public record JwtDto(
    String authorization,
    @JsonIgnore String refreshToken,
    Long expiryDuration,
    List<String> authorities) {}
