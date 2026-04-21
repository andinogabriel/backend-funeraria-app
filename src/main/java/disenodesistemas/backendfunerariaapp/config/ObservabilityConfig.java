package disenodesistemas.backendfunerariaapp.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RequestTracingProperties.class)
public class ObservabilityConfig {}
