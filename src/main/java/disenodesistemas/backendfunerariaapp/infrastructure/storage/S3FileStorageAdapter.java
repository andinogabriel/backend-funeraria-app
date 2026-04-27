package disenodesistemas.backendfunerariaapp.infrastructure.storage;

import disenodesistemas.backendfunerariaapp.application.model.FilePayload;
import disenodesistemas.backendfunerariaapp.application.port.out.FileStoragePort;
import disenodesistemas.backendfunerariaapp.config.S3BucketProperties;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.spi.LoggingEventBuilder;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
public class S3FileStorageAdapter implements FileStoragePort {
  private static final String FILE_SEPARATOR = "/";

  private final S3Client s3;
  private final S3BucketProperties s3BucketProperties;

  public S3FileStorageAdapter(final S3Client s3, final S3BucketProperties s3BucketProperties) {
    this.s3 = s3;
    this.s3BucketProperties = s3BucketProperties;
  }

  @Override
  @Transactional
  public String store(final Object object, final FilePayload file) {
    fileIsEmpty(file);
    isAnImage(file);
    final String folderName = getFolderName(object);
    final String filename = generateFileName(file);
    final String key = folderName + FILE_SEPARATOR + filename;
    logUploadStarted(object, file, key);
    try {
      final PutObjectRequest request =
          PutObjectRequest.builder()
              .bucket(s3BucketProperties.name())
              .key(key)
              .contentType(file.contentType())
              .build();
      s3.putObject(request, RequestBody.fromBytes(file.content()));
      logStorageCompleted("file.storage.upload.completed", "key", key);
      return constructFileUrl(folderName, filename);
    } catch (S3Exception ex) {
      logStorageFailure(ex, "file.storage.upload.failed", "key", key);
      throw new AppException("s3bucket.error.upload.file", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public byte[] download(final String path, final String key) {
    try {
      logStorageStarted("file.storage.download.started", "requestedBucket", path, "key", key);
      final ResponseBytes<?> bytes =
          s3.getObjectAsBytes(GetObjectRequest.builder().bucket(path).key(key).build());
      log.atInfo()
          .addKeyValue("event", "file.storage.download.completed")
          .addKeyValue("provider", "s3")
          .addKeyValue("bucket", path)
          .addKeyValue("key", key)
          .addKeyValue("size", bytes.asByteArray().length)
          .log("file.storage.download.completed");
      return bytes.asByteArray();
    } catch (S3Exception ex) {
      logStorageFailure(ex, "file.storage.download.failed", "requestedBucket", path, "key", key);
      throw new AppException("s3bucket.error.download.file", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public void deleteFiles(final Object object) {
    final String folderName = resolveStoredPrefix(object);
    if (StringUtils.isBlank(folderName)) {
      log.atWarn()
          .addKeyValue("event", "file.storage.delete.skipped")
          .addKeyValue("provider", "s3")
          .addKeyValue("objectType", object.getClass().getSimpleName())
          .addKeyValue("reason", "missing_stored_prefix")
          .log("file.storage.delete.skipped");
      return;
    }
    try {
      logStorageStarted(
          "file.storage.delete.started",
          "prefix",
          folderName,
          "objectType",
          object.getClass().getSimpleName());
      final var files =
          s3.listObjectsV2(
              ListObjectsV2Request.builder()
                  .bucket(s3BucketProperties.name())
                  .prefix(folderName)
                  .build());
      files.contents().forEach(
          file ->
              s3.deleteObject(
                  builder -> builder.bucket(s3BucketProperties.name()).key(file.key())));
      log.atInfo()
          .addKeyValue("event", "file.storage.delete.completed")
          .addKeyValue("provider", "s3")
          .addKeyValue("bucket", s3BucketProperties.name())
          .addKeyValue("prefix", folderName)
          .addKeyValue("deletedCount", files.contents().size())
          .log("file.storage.delete.completed");
    } catch (S3Exception e) {
      logStorageFailure(e, "file.storage.delete.failed", "prefix", folderName);
      throw new AppException("s3bucket.error.delete.file", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private String generateFileName(final FilePayload file) {
    return Objects.requireNonNullElse(file.originalFilename(), UUID.randomUUID().toString())
        .replaceAll("\\s+", "-");
  }

  private String constructFileUrl(final String folderName, final String filename) {
    return s3BucketProperties.url() + folderName + FILE_SEPARATOR + filename;
  }

  private String resolveStoredPrefix(final Object object) {
    if (object instanceof ItemEntity itemEntity) {
      final String storedLink = itemEntity.getItemImageLink();
      if (StringUtils.isBlank(storedLink)) {
        return null;
      }
      final String relativePath = removePrefix(storedLink, s3BucketProperties.url());
      return StringUtils.substringBeforeLast(relativePath, FILE_SEPARATOR);
    }
    return null;
  }

  private String removePrefix(final String value, final String prefix) {
    if (Strings.CS.startsWith(value, prefix)) {
      return value.substring(prefix.length());
    }
    return value;
  }

  private void logUploadStarted(final Object object, final FilePayload file, final String key) {
    log.atInfo()
        .addKeyValue("event", "file.storage.upload.started")
        .addKeyValue("provider", "s3")
        .addKeyValue("bucket", s3BucketProperties.name())
        .addKeyValue("key", key)
        .addKeyValue("objectType", object.getClass().getSimpleName())
        .addKeyValue("contentType", file.contentType())
        .addKeyValue("size", file.size())
        .log("file.storage.upload.started");
  }

  private void logStorageStarted(
      final String event,
      final String firstKey,
      final Object firstValue,
      final String secondKey,
      final Object secondValue) {
    log.atInfo()
        .addKeyValue("event", event)
        .addKeyValue("provider", "s3")
        .addKeyValue("bucket", s3BucketProperties.name())
        .addKeyValue(firstKey, firstValue)
        .addKeyValue(secondKey, secondValue)
        .log(event);
  }

  private void logStorageCompleted(
      final String event, final String detailKey, final Object detailValue) {
    log.atInfo()
        .addKeyValue("event", event)
        .addKeyValue("provider", "s3")
        .addKeyValue("bucket", s3BucketProperties.name())
        .addKeyValue(detailKey, detailValue)
        .log(event);
  }

  private void logStorageFailure(
      final S3Exception exception,
      final String event,
      final String firstKey,
      final Object firstValue) {
    logStorageFailure(exception, event, firstKey, firstValue, null, null);
  }

  private void logStorageFailure(
      final S3Exception exception,
      final String event,
      final String firstKey,
      final Object firstValue,
      final String secondKey,
      final Object secondValue) {
    LoggingEventBuilder builder =
        log.atError()
            .setCause(exception)
            .addKeyValue("event", event)
            .addKeyValue("provider", "s3")
            .addKeyValue("bucket", s3BucketProperties.name())
            .addKeyValue(firstKey, firstValue)
            .addKeyValue("reason", errorReason(exception));

    if (secondKey != null) {
      builder = builder.addKeyValue(secondKey, secondValue);
    }
    builder.log(event);
  }

  private String errorReason(final S3Exception exception) {
    return exception.awsErrorDetails() == null
        ? exception.getMessage()
        : exception.awsErrorDetails().errorMessage();
  }
}
