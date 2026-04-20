package disenodesistemas.backendfunerariaapp.security.jwt;

import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt-token")
public record JwtProperties(
    String secret,
    String authorities,
    Long expirationSeconds,
    String prefix,
    String header,
    @DefaultValue("device_id") String deviceIdClaim,
    @DefaultValue("device_fingerprint") String deviceFingerprintClaim,
    @DefaultValue("device_version") String deviceVersionClaim
) {
}
