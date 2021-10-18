package disenodesistemas.backendfunerariaapp.service.Interface;

import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.apache.http.entity.ContentType.*;

public interface IFileStore {

    String SEPARATOR = "-";

    String save(Object object, MultipartFile file);

    byte[] download(String path, String key);

    default Optional<Map<String, String>> extractMetadata(MultipartFile file) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));
        return Optional.of(metadata);
    }

    default void isAnImage(MultipartFile file) {
        if(!Arrays.asList(IMAGE_JPEG.getMimeType(), IMAGE_PNG.getMimeType(), IMAGE_GIF.getMimeType()).contains(file.getContentType())) {
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
        String objectName = object.getClass().getSimpleName();
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
