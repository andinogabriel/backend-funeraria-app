package disenodesistemas.backendfunerariaapp.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import disenodesistemas.backendfunerariaapp.service.Interface.IFileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Objects;

@Service
public class FileStoreServiceImpl implements IFileStore {

    private final AmazonS3 s3;
    private final MessageSource messageSource;
    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.bucket.url}")
    private String bucketUrl;

    @Autowired
    public FileStoreServiceImpl(AmazonS3 s3, MessageSource messageSource) {
        this.s3 = s3;
        this.messageSource = messageSource;
    }


    @Override
    public String save(Object object, MultipartFile file) {
        fileIsEmpty(file);
        isAnImage(file);

        ObjectMetadata metadata = new ObjectMetadata();
        //Metadata extraction from the file - Grabar metadata del archivo
        extractMetadata(file).ifPresent(map -> {
            if(!map.isEmpty()) {
                map.forEach(metadata::addUserMetadata);
            }
        });

        String folderName = getFolderName(object);

        try {
            String path = String.format("%s/%s", bucketName, folderName);
            String filename = String.format("%s", Objects.requireNonNull(file.getOriginalFilename()).replaceAll("\\s+", SEPARATOR));
            s3.putObject(path, filename, file.getInputStream(), metadata);
            return bucketUrl + folderName + "/" + filename;
        } catch (AmazonServiceException | IOException | IllegalStateException ex) {
            throw new IllegalStateException(messageSource.getMessage(
                    "s3bucket.error.upload.file" + " " + ex.getMessage(), null, Locale.getDefault()
            ));
        }
    }

    @Override
    public byte[] download(String path, String key) {
        try {
            S3Object object = s3.getObject(path, key);
            return IOUtils.toByteArray(object.getObjectContent());
        } catch (AmazonServiceException | IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void deleteFilesFromS3Bucket(Object object) {
        String folderName = getFolderName(object);
        try {
            for(S3ObjectSummary file : s3.listObjects(bucketName, folderName).getObjectSummaries()) {
                s3.deleteObject(bucketName, file.getKey());
            }
        } catch (SdkClientException e) {
            e.getMessage();
        }
    }
}