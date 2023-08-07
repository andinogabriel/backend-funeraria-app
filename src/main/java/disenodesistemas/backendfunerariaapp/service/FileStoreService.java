package disenodesistemas.backendfunerariaapp.service;

import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.apache.http.entity.ContentType.IMAGE_GIF;
import static org.apache.http.entity.ContentType.IMAGE_JPEG;
import static org.apache.http.entity.ContentType.IMAGE_PNG;


public interface FileStoreService {

    String SEPARATOR = "-";

    String save(Object object, MultipartFile file);

    byte[] download(String path, String key);

    default Optional<Map<String, String>> extractMetadata(MultipartFile file) {
        return Optional.of(
                Map.of(
                        "Content-Type", Objects.requireNonNullElse(file.getContentType(), null),
                        "Content-Length", String.valueOf(file.getSize())
                )
        );
    }

    default void isAnImage(MultipartFile file) {
        if(!List.of(IMAGE_JPEG.getMimeType(), IMAGE_PNG.getMimeType(), IMAGE_GIF.getMimeType())
                .contains(file.getContentType())) {
            throw new IllegalStateException("El archivo debe ser una imagen valida (JPEG, JPG, PNG, GIF).");
        }
    }

    default void fileIsEmpty(MultipartFile file) {
        if(file.isEmpty()) {
            throw new IllegalStateException("No se puede subir un archivo vacio.");
        }
    }

    void deleteFilesFromS3Bucket(Object object);

    default String getFolderName(Object object) {
        final String objectName = object.getClass().getSimpleName();
        Field privateObjectId = null;
        Long objectId = null;
        try {
            privateObjectId = object.getClass().getDeclaredField("id");
            privateObjectId.setAccessible(true);
            objectId = (Long) privateObjectId.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.getMessage();
        }
        return objectName + SEPARATOR + objectId;
    }

}
