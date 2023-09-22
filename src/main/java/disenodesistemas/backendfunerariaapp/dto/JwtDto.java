package disenodesistemas.backendfunerariaapp.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class JwtDto {
    String authorization;
    String refreshToken;
    String tokenType = "Bearer";
    Long expiryDuration;
    Collection<? extends GrantedAuthority> authorities;
}
