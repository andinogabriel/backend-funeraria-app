package disenodesistemas.backendfunerariaapp.config;

import java.nio.file.Path;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local")
@EnableConfigurationProperties(LocalStorageProperties.class)
public class LocalStorageWebConfig implements WebMvcConfigurer {

  private final LocalStorageProperties localStorageProperties;

  public LocalStorageWebConfig(final LocalStorageProperties localStorageProperties) {
    this.localStorageProperties = localStorageProperties;
  }

  @Override
  public void addResourceHandlers(final ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler("/files/**")
        .addResourceLocations(
            Path.of(localStorageProperties.rootPath()).toAbsolutePath().normalize().toUri().toString());
  }
}
