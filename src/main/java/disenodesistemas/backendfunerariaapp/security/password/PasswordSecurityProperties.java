package disenodesistemas.backendfunerariaapp.security.password;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security.password")
public record PasswordSecurityProperties(
    @NotBlank(message = "{security.password.pepper.required}") String pepper,
    @DefaultValue("16") @Min(8) int saltLength,
    @DefaultValue("32") @Min(16) int hashLength,
    @DefaultValue("1") @Min(1) int parallelism,
    @DefaultValue("65536") @Min(8192) int memoryKb,
    @DefaultValue("3") @Min(1) int iterations) {}
