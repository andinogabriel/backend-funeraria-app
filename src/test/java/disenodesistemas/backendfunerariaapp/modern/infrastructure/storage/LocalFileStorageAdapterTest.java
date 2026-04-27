package disenodesistemas.backendfunerariaapp.modern.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import disenodesistemas.backendfunerariaapp.application.model.FilePayload;
import disenodesistemas.backendfunerariaapp.config.LocalStorageProperties;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.infrastructure.storage.LocalFileStorageAdapter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("LocalFileStorageAdapter")
class LocalFileStorageAdapterTest {

  @TempDir Path tempDir;

  @Test
  @DisplayName(
      "Given a valid image payload when the file is stored then the adapter writes it under the configured root path and returns a public URL")
  void givenAValidImagePayloadWhenTheFileIsStoredThenTheAdapterWritesItUnderTheConfiguredRootPathAndReturnsAPublicUrl()
      throws Exception {
    final LocalFileStorageAdapter adapter =
        new LocalFileStorageAdapter(
            new LocalStorageProperties(tempDir.toString(), "http://localhost:8081/files/"));
    final FilePayload filePayload =
        new FilePayload("ataud premium.png", "image/png", "png-content".getBytes());

    final String publicUrl = adapter.store(new ItemEntity(), filePayload);

    assertThat(publicUrl).startsWith("http://localhost:8081/files/ItemEntity-");
    assertThat(publicUrl).endsWith("/ataud-premium.png");
    final String relativePath = publicUrl.replace("http://localhost:8081/files/", "");
    assertThat(Files.readString(tempDir.resolve(relativePath))).isEqualTo("png-content");
  }

  @Test
  @DisplayName(
      "Given an unsupported file type when the file is stored then the adapter rejects the upload with an application error")
  void givenAnUnsupportedFileTypeWhenTheFileIsStoredThenTheAdapterRejectsTheUploadWithAnApplicationError() {
    final LocalFileStorageAdapter adapter =
        new LocalFileStorageAdapter(
            new LocalStorageProperties(tempDir.toString(), "http://localhost:8081/files/"));
    final FilePayload filePayload =
        new FilePayload("documento.pdf", "application/pdf", "pdf-content".getBytes());

    assertThatThrownBy(() -> adapter.store(new ItemEntity(), filePayload))
        .isInstanceOf(AppException.class)
        .extracting("message")
        .isEqualTo("file.storage.error.invalid.image");
  }

  @Test
  @DisplayName(
      "Given an item with a previously stored local image when its files are deleted then the adapter removes the backing folder from disk")
  void givenAnItemWithAPreviouslyStoredLocalImageWhenItsFilesAreDeletedThenTheAdapterRemovesTheBackingFolderFromDisk()
      throws Exception {
    final LocalFileStorageAdapter adapter =
        new LocalFileStorageAdapter(
            new LocalStorageProperties(tempDir.toString(), "http://localhost:8081/files/"));
    final FilePayload filePayload =
        new FilePayload("ataud.png", "image/png", "png-content".getBytes());
    final ItemEntity itemEntity = new ItemEntity();

    final String publicUrl = adapter.store(itemEntity, filePayload);
    itemEntity.setItemImageLink(publicUrl);
    final Path storedFolder = tempDir.resolve(publicUrl.replace("http://localhost:8081/files/", ""));

    adapter.deleteFiles(itemEntity);

    assertThat(Files.exists(storedFolder.getParent())).isFalse();
  }
}
