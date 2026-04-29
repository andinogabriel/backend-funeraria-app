package disenodesistemas.backendfunerariaapp.infrastructure.security.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security.request")
public record SecurityRequestProperties(
    @DefaultValue("X-Device-Id") @NotBlank String deviceIdHeader,
    @DefaultValue("Idempotency-Key") @NotBlank String idempotencyKeyHeader,
    @NotBlank String fingerprintSecret) {}
