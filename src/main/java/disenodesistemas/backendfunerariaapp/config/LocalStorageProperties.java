package disenodesistemas.backendfunerariaapp.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.storage.local")
public record LocalStorageProperties(@NotBlank String rootPath, @NotBlank String publicBaseUrl) {}
