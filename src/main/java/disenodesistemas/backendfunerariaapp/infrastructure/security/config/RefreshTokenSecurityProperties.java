package disenodesistemas.backendfunerariaapp.infrastructure.security.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security.refresh-token")
public record RefreshTokenSecurityProperties(
    @DefaultValue("604800") @Min(60) long expirationSeconds,
    @DefaultValue("32") @Min(16) int randomBytesLength) {}
