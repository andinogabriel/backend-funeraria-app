package disenodesistemas.backendfunerariaapp.security.threat;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security.threat-protection")
public record ThreatProtectionProperties(
    @DefaultValue("10") @Min(1) int failedLoginBlacklistThreshold,
    @DefaultValue("2") @Min(1) int suspiciousRequestThreshold,
    @DefaultValue("3600") @Min(60) long blacklistSeconds,
    @DefaultValue("true") boolean immediateBlacklistOnDeviceMismatch) {}
