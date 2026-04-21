package disenodesistemas.backendfunerariaapp.config;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@ConditionalOnProperty(prefix = "app.web.cors", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(WebCorsProperties.class)
public class WebCorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      final WebCorsProperties webCorsProperties) {
    final CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedMethods(List.of(CorsConfiguration.ALL));
    configuration.setAllowedHeaders(List.of(CorsConfiguration.ALL));
    configuration.setAllowedOriginPatterns(webCorsProperties.allowedOriginPatterns());

    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
