package disenodesistemas.backendfunerariaapp.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.bootstrap.admin")
public record DevelopmentAdminProperties(
    @DefaultValue("true") boolean enabled,
    @DefaultValue("admin@funeraria.local") @NotBlank String email,
    @DefaultValue("Admin123!") @NotBlank String password,
    @DefaultValue("Admin") @NotBlank String firstName,
    @DefaultValue("Funeraria") @NotBlank String lastName) {}
