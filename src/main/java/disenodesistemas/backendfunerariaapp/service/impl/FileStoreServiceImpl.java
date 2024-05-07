package disenodesistemas.backendfunerariaapp.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.service.FileStoreService;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class FileStoreServiceImpl implements FileStoreService {

  private static final String FILE_SEPARATOR = "/";
  private static final String UPLOAD_ERROR_MESSAGE = "Error uploading file to S3: ";
  private static final String DOWNLOAD_ERROR_MESSAGE = "Error downloading file from S3: ";
  private static final String DELETE_ERROR_MESSAGE = "Error deleting file from S3: ";

  private final AmazonS3 s3;
  private final String bucketName;
  private final String bucketUrl;

  public FileStoreServiceImpl(
      final AmazonS3 s3,
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

    final ObjectMetadata metadata = createMetadata(file);
    final String folderName = getFolderName(object);
    final String filename = generateFileName(file);

    try {
      final String path = bucketName + FILE_SEPARATOR + folderName;
      s3.putObject(path, filename, file.getInputStream(), metadata);
      return constructFileUrl(folderName, filename);
    } catch (AmazonServiceException | IOException ex) {
      throw new IllegalStateException(UPLOAD_ERROR_MESSAGE + ex.getMessage(), ex);
    }
  }

  @Override
  public byte[] download(final String path, final String key) {
    try (final S3ObjectInputStream objectContent = s3.getObject(path, key).getObjectContent()) {
      return IOUtils.toByteArray(objectContent);
    } catch (AmazonServiceException | IOException ex) {
      throw new IllegalStateException(DOWNLOAD_ERROR_MESSAGE + ex.getMessage(), ex);
    }
  }

  @Override
  public void deleteFilesFromS3Bucket(final Object object) {
    final String folderName = getFolderName(object);
    try {
      final List<S3ObjectSummary> files =
          s3.listObjects(bucketName, folderName).getObjectSummaries();
      files.forEach(file -> s3.deleteObject(bucketName, file.getKey()));
    } catch (SdkClientException e) {
      throw new AppException(
          DELETE_ERROR_MESSAGE + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private ObjectMetadata createMetadata(final MultipartFile file) {
    final ObjectMetadata metadata = new ObjectMetadata();
    extractMetadata(file).ifPresent(map -> map.forEach(metadata::addUserMetadata));
    return metadata;
  }

  private String generateFileName(final MultipartFile file) {
    return Objects.requireNonNullElse(file.getOriginalFilename(), UUID.randomUUID().toString())
        .replaceAll("\\s+", SEPARATOR);
  }

  private String constructFileUrl(String folderName, String filename) {
    return bucketUrl + folderName + FILE_SEPARATOR + filename;
  }
}
