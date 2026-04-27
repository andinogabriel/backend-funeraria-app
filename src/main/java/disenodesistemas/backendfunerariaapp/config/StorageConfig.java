package disenodesistemas.backendfunerariaapp.config;

import disenodesistemas.backendfunerariaapp.application.port.out.FileStoragePort;
import disenodesistemas.backendfunerariaapp.infrastructure.storage.LocalFileStorageAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.storage.S3FileStorageAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class StorageConfig {

  @Configuration
  @ConditionalOnProperty(name = "app.storage.provider", havingValue = "local")
  @EnableConfigurationProperties(LocalStorageProperties.class)
  static class LocalStorageConfig {

    @Bean
    FileStoragePort fileStoragePort(final LocalStorageProperties localStorageProperties) {
      return new LocalFileStorageAdapter(localStorageProperties);
    }
  }

  @Configuration
  @ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3", matchIfMissing = true)
  static class S3StorageConfig {

    @Bean
    FileStoragePort fileStoragePort(
        final S3Client s3Client, final S3BucketProperties s3BucketProperties) {
      return new S3FileStorageAdapter(s3Client, s3BucketProperties);
    }
  }
}
