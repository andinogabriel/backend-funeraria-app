package disenodesistemas.backendfunerariaapp.web.dto;

import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@Jacksonized
public record JwtDto(String authorization, String refreshToken, Long expiryDuration, List<String> authorities) {}
