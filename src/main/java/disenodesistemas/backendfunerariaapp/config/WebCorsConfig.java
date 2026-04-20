package disenodesistemas.backendfunerariaapp.config;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(prefix = "app.web.cors", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(WebCorsProperties.class)
public class WebCorsConfig {

  @Bean
  public WebMvcConfigurer corsConfigurer(final WebCorsProperties webCorsProperties) {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(final @NonNull CorsRegistry registry) {
        registry
            .addMapping("/**")
            .allowedMethods(CorsConfiguration.ALL)
            .allowedHeaders(CorsConfiguration.ALL)
            .allowedOriginPatterns(webCorsProperties.allowedOriginPatterns().toArray(String[]::new));
      }
    };
  }
}
