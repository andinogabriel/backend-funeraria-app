package disenodesistemas.backendfunerariaapp.infrastructure.storage;

import disenodesistemas.backendfunerariaapp.application.model.FilePayload;
import disenodesistemas.backendfunerariaapp.application.port.out.FileStoragePort;
import disenodesistemas.backendfunerariaapp.config.LocalStorageProperties;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

@Slf4j
public class LocalFileStorageAdapter implements FileStoragePort {

  private static final String FILE_SEPARATOR = "/";

  private final LocalStorageProperties localStorageProperties;
  private final Path rootPath;

  public LocalFileStorageAdapter(final LocalStorageProperties localStorageProperties) {
    this.localStorageProperties = localStorageProperties;
    this.rootPath = Path.of(localStorageProperties.rootPath()).toAbsolutePath().normalize();
  }

  @Override
  public String store(final Object object, final FilePayload file) {
    fileIsEmpty(file);
    isAnImage(file);
    final String folderName = getFolderName(object);
    final String filename = sanitizeFilename(file.originalFilename());
    final Path folderPath = resolvePath(folderName);
    final Path filePath = resolvePath(folderName + FILE_SEPARATOR + filename);
    try {
      Files.createDirectories(folderPath);
      log.atInfo()
          .addKeyValue("event", "file.storage.upload.started")
          .addKeyValue("provider", "local")
          .addKeyValue("rootPath", rootPath)
          .addKeyValue("filePath", filePath)
          .addKeyValue("objectType", object.getClass().getSimpleName())
          .addKeyValue("contentType", file.contentType())
          .addKeyValue("size", file.size())
          .log("file.storage.upload.started");
      Files.write(
          filePath,
          file.content(),
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING,
          StandardOpenOption.WRITE);
      log.atInfo()
          .addKeyValue("event", "file.storage.upload.completed")
          .addKeyValue("provider", "local")
          .addKeyValue("rootPath", rootPath)
          .addKeyValue("filePath", filePath)
          .log("file.storage.upload.completed");
      return buildPublicUrl(folderName, filename);
    } catch (IOException ex) {
      log.atError()
          .setCause(ex)
          .addKeyValue("event", "file.storage.upload.failed")
          .addKeyValue("provider", "local")
          .addKeyValue("rootPath", rootPath)
          .addKeyValue("filePath", filePath)
          .addKeyValue("reason", ex.getMessage())
          .log("file.storage.upload.failed");
      throw new AppException("s3bucket.error.upload.file", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public byte[] download(final String path, final String key) {
    final Path filePath = resolvePath(path + FILE_SEPARATOR + key);
    try {
      log.atInfo()
          .addKeyValue("event", "file.storage.download.started")
          .addKeyValue("provider", "local")
          .addKeyValue("filePath", filePath)
          .log("file.storage.download.started");
      final byte[] content = Files.readAllBytes(filePath);
      log.atInfo()
          .addKeyValue("event", "file.storage.download.completed")
          .addKeyValue("provider", "local")
          .addKeyValue("filePath", filePath)
          .addKeyValue("size", content.length)
          .log("file.storage.download.completed");
      return content;
    } catch (IOException ex) {
      log.atError()
          .setCause(ex)
          .addKeyValue("event", "file.storage.download.failed")
          .addKeyValue("provider", "local")
          .addKeyValue("filePath", filePath)
          .addKeyValue("reason", ex.getMessage())
          .log("file.storage.download.failed");
      throw new AppException("s3bucket.error.download.file", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public void deleteFiles(final Object object) {
    final String storedPrefix = resolveStoredPrefix(object);
    if (StringUtils.isBlank(storedPrefix)) {
      log.atWarn()
          .addKeyValue("event", "file.storage.delete.skipped")
          .addKeyValue("provider", "local")
          .addKeyValue("objectType", object.getClass().getSimpleName())
          .addKeyValue("reason", "missing_stored_prefix")
          .log("file.storage.delete.skipped");
      return;
    }

    final Path targetPath = resolvePath(storedPrefix);
    if (Files.notExists(targetPath)) {
      log.atWarn()
          .addKeyValue("event", "file.storage.delete.skipped")
          .addKeyValue("provider", "local")
          .addKeyValue("filePath", targetPath)
          .addKeyValue("reason", "path_not_found")
          .log("file.storage.delete.skipped");
      return;
    }

    try (var paths = Files.walk(targetPath)) {
      log.atInfo()
          .addKeyValue("event", "file.storage.delete.started")
          .addKeyValue("provider", "local")
          .addKeyValue("filePath", targetPath)
          .log("file.storage.delete.started");
      paths.sorted(Comparator.reverseOrder()).forEach(this::deleteQuietly);
      log.atInfo()
          .addKeyValue("event", "file.storage.delete.completed")
          .addKeyValue("provider", "local")
          .addKeyValue("filePath", targetPath)
          .log("file.storage.delete.completed");
    } catch (IOException ex) {
      log.atError()
          .setCause(ex)
          .addKeyValue("event", "file.storage.delete.failed")
          .addKeyValue("provider", "local")
          .addKeyValue("filePath", targetPath)
          .addKeyValue("reason", ex.getMessage())
          .log("file.storage.delete.failed");
      throw new AppException("s3bucket.error.delete.file", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private void deleteQuietly(final Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (IOException ex) {
      log.atError()
          .setCause(ex)
          .addKeyValue("event", "file.storage.delete.failed")
          .addKeyValue("provider", "local")
          .addKeyValue("filePath", path)
          .addKeyValue("reason", ex.getMessage())
          .log("file.storage.delete.failed");
      throw new AppException("s3bucket.error.delete.file", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private Path resolvePath(final String relativePath) {
    final Path resolved = rootPath.resolve(relativePath).normalize();
    if (!resolved.startsWith(rootPath)) {
      throw new AppException("s3bucket.error.delete.file", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return resolved;
  }

  private String sanitizeFilename(final String originalFilename) {
    final String fallback = UUID.randomUUID().toString();
    final String candidate =
        StringUtils.replace(StringUtils.defaultIfBlank(originalFilename, fallback), "\\", FILE_SEPARATOR);
    final String baseName =
        StringUtils.contains(candidate, FILE_SEPARATOR)
            ? StringUtils.substringAfterLast(candidate, FILE_SEPARATOR)
            : candidate;
    return StringUtils.replaceChars(baseName, ' ', '-');
  }

  private String buildPublicUrl(final String folderName, final String filename) {
    return StringUtils.appendIfMissing(localStorageProperties.publicBaseUrl(), FILE_SEPARATOR)
        + folderName
        + FILE_SEPARATOR
        + filename;
  }

  private String resolveStoredPrefix(final Object object) {
    if (object instanceof ItemEntity itemEntity) {
      final String storedLink = itemEntity.getItemImageLink();
      if (StringUtils.isBlank(storedLink)) {
        return null;
      }
      final String relativePath =
          removePrefix(
              storedLink, StringUtils.appendIfMissing(localStorageProperties.publicBaseUrl(), FILE_SEPARATOR));
      return StringUtils.substringBeforeLast(relativePath, FILE_SEPARATOR);
    }
    return null;
  }

  private String removePrefix(final String value, final String prefix) {
    if (StringUtils.isEmpty(value) || StringUtils.isEmpty(prefix) || !value.startsWith(prefix)) {
      return value;
    }
    return value.substring(prefix.length());
  }
}
