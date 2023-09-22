package disenodesistemas.backendfunerariaapp.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.service.FileStoreService;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStoreServiceImpl implements FileStoreService {

    private final AmazonS3 s3;
    private final String bucketName;
    private final String bucketUrl;

    public FileStoreServiceImpl(final AmazonS3 s3,
                                @Value("${aws.s3.bucket.name}") final String bucketName,
                                @Value("${aws.s3.bucket.url}") final String bucketUrl) {
        this.s3 = s3;
        this.bucketName = bucketName;
        this.bucketUrl = bucketUrl;
    }

    @Override
    @Transactional
    public String save(final Object object, final MultipartFile file) {
        fileIsEmpty(file);
        isAnImage(file);

        val metadata = new ObjectMetadata();
        extractMetadata(file).ifPresent(map -> {
            if(!map.isEmpty()) {
                map.forEach(metadata::addUserMetadata);
            }
        });

        final String folderName = getFolderName(object);

        try {
            val path = String.format("%s/%s", bucketName, folderName);
            val filename = String.format("%s", Objects.requireNonNullElse(file.getOriginalFilename(), UUID.randomUUID().toString())
                    .replaceAll("\\s+", SEPARATOR));
            s3.putObject(path, filename, file.getInputStream(), metadata);
            return bucketUrl + folderName + "/" + filename;
        } catch (AmazonServiceException | IOException | IllegalStateException ex) {
            throw new IllegalStateException("s3bucket.error.upload.file" + " " + ex.getMessage());
        }
    }

    @Override
    public byte[] download(final String path, final String key) {
        try {
            final S3Object object = s3.getObject(path, key);
            return IOUtils.toByteArray(object.getObjectContent());
        } catch (AmazonServiceException | IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void deleteFilesFromS3Bucket(final Object object) {
        final String folderName = getFolderName(object);
        try {
            final List<S3ObjectSummary> files = s3.listObjects(bucketName, folderName).getObjectSummaries();
            files.forEach(file -> s3.deleteObject(bucketName, file.getKey()));
        } catch (SdkClientException e) {
            throw new AppException("Error al eliminar la imagen.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}