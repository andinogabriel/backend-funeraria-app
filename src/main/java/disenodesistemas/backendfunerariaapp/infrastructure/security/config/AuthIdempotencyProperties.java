package disenodesistemas.backendfunerariaapp.infrastructure.security.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security.idempotency")
public record AuthIdempotencyProperties(@DefaultValue("120") @Min(10) long ttlSeconds) {}
