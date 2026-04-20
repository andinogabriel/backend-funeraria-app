package disenodesistemas.backendfunerariaapp.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.web.cors")
public record WebCorsProperties(
    @DefaultValue("false") boolean enabled,
    @DefaultValue({"http://localhost:3000", "http://localhost:5173"}) List<String> allowedOriginPatterns) {}
