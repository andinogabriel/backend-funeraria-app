package disenodesistemas.backendfunerariaapp.modern.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import disenodesistemas.backendfunerariaapp.application.port.out.FileStoragePort;
import disenodesistemas.backendfunerariaapp.config.S3BucketProperties;
import disenodesistemas.backendfunerariaapp.config.StorageConfig;
import disenodesistemas.backendfunerariaapp.infrastructure.storage.LocalFileStorageAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.storage.S3FileStorageAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.services.s3.S3Client;

@DisplayName("StorageConfig")
class StorageConfigTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner().withUserConfiguration(StorageConfig.class);

  @Test
  @DisplayName(
      "Given local storage provider when the context starts then it registers the local storage port")
  void givenLocalStorageProviderWhenTheContextStartsThenItRegistersTheLocalStoragePort() {
    contextRunner
        .withPropertyValues(
            "app.storage.provider=local",
            "app.storage.local.root-path=target/test-storage",
            "app.storage.local.public-base-url=http://localhost:8081/files/")
        .run(
            context -> {
              assertThat(context).hasSingleBean(FileStoragePort.class);
              assertThat(context).hasSingleBean(LocalFileStorageAdapter.class);
              assertThat(context).doesNotHaveBean(S3FileStorageAdapter.class);
            });
  }

  @Test
  @DisplayName(
      "Given S3 storage provider when the context starts then it registers the S3 storage port")
  void givenS3StorageProviderWhenTheContextStartsThenItRegistersTheS3StoragePort() {
    contextRunner
        .withBean(S3Client.class, () -> mock(S3Client.class))
        .withBean(
            S3BucketProperties.class,
            () -> new S3BucketProperties("funeraria-bucket", "https://cdn.example.com/"))
        .withPropertyValues("app.storage.provider=s3")
        .run(
            context -> {
              assertThat(context).hasSingleBean(FileStoragePort.class);
              assertThat(context).hasSingleBean(S3FileStorageAdapter.class);
              assertThat(context).doesNotHaveBean(LocalFileStorageAdapter.class);
            });
  }
}
