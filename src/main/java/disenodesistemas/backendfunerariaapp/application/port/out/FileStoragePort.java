package disenodesistemas.backendfunerariaapp.application.port.out;

import static org.springframework.http.MediaType.IMAGE_GIF_VALUE;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

import disenodesistemas.backendfunerariaapp.application.model.FilePayload;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public interface FileStoragePort {

  String SEPARATOR = "-";
  List<String> ALLOWED_IMAGE_TYPES =
      List.of(
          IMAGE_JPEG_VALUE, // "image/jpeg"
          IMAGE_PNG_VALUE, // "image/png"
          IMAGE_GIF_VALUE // "image/gif"
          );

  String store(Object object, FilePayload file);

  byte[] download(String path, String key);

  default Optional<Map<String, String>> extractMetadata(final FilePayload file) {
    return Optional.of(
        Map.of(
            "Content-Type", Objects.requireNonNullElse(file.contentType(), null),
            "Content-Length", String.valueOf(file.size())));
  }

  default void isAnImage(final FilePayload file) {
    if (!ALLOWED_IMAGE_TYPES.contains(file.contentType())) {
      throw new AppException("file.storage.error.invalid.image", HttpStatus.BAD_REQUEST);
    }
  }

  default void fileIsEmpty(final FilePayload file) {
    if (file.content() == null || file.content().length == 0) {
      throw new AppException("file.storage.error.empty", HttpStatus.BAD_REQUEST);
    }
  }

  void deleteFiles(Object object);

  default String getFolderName(Object object) {
    final String objectName = object.getClass().getSimpleName();
    final String uniqueId = UUID.randomUUID().toString();
    return objectName + SEPARATOR + uniqueId;
  }
}
