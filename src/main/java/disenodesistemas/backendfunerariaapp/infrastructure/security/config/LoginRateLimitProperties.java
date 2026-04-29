package disenodesistemas.backendfunerariaapp.infrastructure.security.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security.login-rate-limit")
public record LoginRateLimitProperties(
    @DefaultValue("5") @Min(1) int maxFailures,
    @DefaultValue("10") @Min(1) int baseLockSeconds,
    @DefaultValue("300") @Min(1) int maxLockSeconds,
    @DefaultValue("15") @Min(1) int windowMinutes) {}
