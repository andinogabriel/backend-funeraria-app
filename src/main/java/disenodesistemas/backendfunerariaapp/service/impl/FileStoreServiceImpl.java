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
import java.util.Locale;

@Service
public class FileStoreServiceImpl implements IFileStore {

    private final AmazonS3 s3;
    private final MessageSource messageSource;
    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Autowired
    public FileStoreServiceImpl(AmazonS3 s3, MessageSource messageSource) {
        this.s3 = s3;
        this.messageSource = messageSource;
    }


    @Override
    public void save(String path, String fileName, MultipartFile file) {
        //Validate if the file is empty - Chequear si la imagen no esta vacia
        fileIsEmpty(file);
        //Validate if the file corresponds to an image - Chequear si el archivo es una imagen valida
        isAnImage(file);

        ObjectMetadata metadata = new ObjectMetadata();
        //Metadata extraction from the file - Grabar metadata del archivo
        extractMetadata(file).ifPresent(map -> {
            if (!map.isEmpty()) {
                map.forEach(metadata::addUserMetadata);
            }
        });

        try {
            s3.putObject(path, fileName, file.getInputStream(), metadata);
        } catch (AmazonServiceException | IOException ex) {
            throw new IllegalStateException(messageSource.getMessage(
                    "s3bucket.error.upload.file" + " " + ex, null, Locale.getDefault()
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
    public void deleteFilesFromS3Bucket(String fileUrl) {
        try {
            for(S3ObjectSummary file : s3.listObjects(bucketName, fileUrl).getObjectSummaries()) {
                s3.deleteObject(bucketName, file.getKey());
            }
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }
}