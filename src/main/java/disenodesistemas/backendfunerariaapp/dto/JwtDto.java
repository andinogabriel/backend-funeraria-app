package disenodesistemas.backendfunerariaapp.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

@Value
@Jacksonized
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JwtDto {
    String authorization;
    String refreshToken;
    String tokenType = "Bearer";
    Long expiryDuration;
    List<String> authorities;
}
