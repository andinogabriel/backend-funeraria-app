package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class TokenRefreshRequestDto {
    @NotBlank(message = "{refresh.token.error.blank.token}")
    String refreshToken;
}
