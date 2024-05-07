package disenodesistemas.backendfunerariaapp.service;

import static org.springframework.http.MediaType.IMAGE_GIF_VALUE;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface FileStoreService {

  String SEPARATOR = "-";
  List<String> ALLOWED_IMAGE_TYPES =
      List.of(
          IMAGE_JPEG_VALUE, // "image/jpeg"
          IMAGE_PNG_VALUE, // "image/png"
          IMAGE_GIF_VALUE // "image/gif"
          );

  String save(Object object, MultipartFile file);

  byte[] download(String path, String key);

  default Optional<Map<String, String>> extractMetadata(MultipartFile file) {
    return Optional.of(
        Map.of(
            "Content-Type", Objects.requireNonNullElse(file.getContentType(), null),
            "Content-Length", String.valueOf(file.getSize())));
  }

  default void isAnImage(final MultipartFile file) {
    if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
      throw new IllegalStateException(
          "El archivo debe ser una imagen valida (JPEG, JPG, PNG, GIF).");
    }
  }

  default void fileIsEmpty(MultipartFile file) {
    if (file.isEmpty()) {
      throw new IllegalStateException("No se puede subir un archivo vacio.");
    }
  }

  void deleteFilesFromS3Bucket(Object object);

  default String getFolderName(Object object) {
    final String objectName = object.getClass().getSimpleName();
    final String uniqueId = UUID.randomUUID().toString();
    return objectName + SEPARATOR + uniqueId;
  }
}
