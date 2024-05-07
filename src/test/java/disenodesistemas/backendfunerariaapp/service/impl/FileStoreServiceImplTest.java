package disenodesistemas.backendfunerariaapp.service.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileStoreServiceImplTest {

  @Mock private AmazonS3 s3;
  @InjectMocks private FileStoreServiceImpl sut;
  private final String bucketName = "test-bucket";
  private final String bucketUrl = "http://localhost/";
  private static MultipartFile file;
  private static AmazonServiceException amazonServiceException;
  private static String folderName;
  private static String filename;
  private static String path;
  private static Object objectToUploadImageUrl;

  @BeforeEach
  void setUp() {
    file =
        new MockMultipartFile(
            "file", "test.jpg", IMAGE_JPEG_VALUE, "test image content".getBytes());
    amazonServiceException = new AmazonServiceException("Error");
    amazonServiceException.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    amazonServiceException.setErrorCode("InternalError");
    sut =
        new FileStoreServiceImpl(s3, bucketName, bucketUrl) {
          @Override
          public String getFolderName(final Object object) {
            return "TestObject-12345";
          }
        };
    objectToUploadImageUrl = new Object();
    folderName = sut.getFolderName(objectToUploadImageUrl);
    filename = file.getOriginalFilename();
    path = bucketName + "/" + folderName;
  }

  @Test
  void save() {
    final String fileUrl = sut.save(objectToUploadImageUrl, file);

    assertAll(
        () -> assertNotNull(fileUrl),
        () -> assertTrue(fileUrl.contains(bucketUrl)),
        () -> assertTrue(fileUrl.contains(folderName)),
        () -> assertTrue(fileUrl.contains(filename)));
    then(s3)
        .should(times(1))
        .putObject(
            eq(path), eq(filename), any(ByteArrayInputStream.class), any(ObjectMetadata.class));
  }

  @Test
  void save_shouldThrowException_whenUploadFails() throws IOException {
    final String ERROR_MESSAGE = "Error uploading file to S3: Error";

    willThrow(amazonServiceException)
        .given(s3)
        .putObject(eq(path), eq(filename), any(InputStream.class), any(ObjectMetadata.class));

    final IllegalStateException thrownException =
        assertThrows(IllegalStateException.class, () -> sut.save(objectToUploadImageUrl, file));

    assertAll(
        () -> assertTrue(thrownException.getMessage().contains(ERROR_MESSAGE)),
        () -> assertNotNull(thrownException.getCause()),
        () -> assertEquals(amazonServiceException, thrownException.getCause()));
    then(s3)
        .should(times(1))
        .putObject(eq(path), eq(filename), any(InputStream.class), any(ObjectMetadata.class));
  }

  @Test
  void download_shouldReturnFileContent_whenFileExists() throws IOException {
    final String path = "some/path";
    final String key = "file.jpg";
    final byte[] expectedContent = "file content".getBytes();
    final S3Object s3Object = new S3Object();
    s3Object.setObjectContent(new ByteArrayInputStream(expectedContent));
    given(s3.getObject(path, key)).willReturn(s3Object);

    final byte[] actualContent = sut.download(path, key);

    assertAll(
        () -> assertNotNull(actualContent),
        () -> assertArrayEquals(expectedContent, actualContent));
  }

  @Test
  void download_shouldThrowException_whenS3ServiceFails() {
    final String DOWNLOAD_ERROR_MESSAGE = "Error downloading file from S3: ";
    final String path = "some/path";
    final String key = "file.jpg";
    given(s3.getObject(path, key)).willThrow(amazonServiceException);

    final IllegalStateException thrownException =
        assertThrows(IllegalStateException.class, () -> sut.download(path, key));

    assertAll(
        () -> assertEquals(amazonServiceException, thrownException.getCause()),
        () ->
            assertEquals(
                DOWNLOAD_ERROR_MESSAGE + amazonServiceException.getMessage(),
                thrownException.getMessage()));
  }

  @Test
  void deleteFilesFromS3Bucket_shouldDeleteFiles_whenFilesExist() {
    final S3ObjectSummary summary1 = new S3ObjectSummary();
    final S3ObjectSummary summary2 = new S3ObjectSummary();
    summary1.setKey(folderName + "/file1.jpg");
    summary2.setKey(folderName + "/file2.jpg");
    final List<S3ObjectSummary> objectSummaries = List.of(summary1, summary2);
    final ObjectListing objectListing = mock(ObjectListing.class);

    given(objectListing.getObjectSummaries()).willReturn(objectSummaries);
    given(s3.listObjects(bucketName, folderName)).willReturn(objectListing);

    sut.deleteFilesFromS3Bucket(objectToUploadImageUrl);

    then(s3).should(times(1)).deleteObject(bucketName, summary1.getKey());
    then(s3).should(times(1)).deleteObject(bucketName, summary2.getKey());
  }

  @Test
  void deleteFilesFromS3Bucket_shouldThrowAppException_whenSdkClientExceptionOccurs() {
    final String DELETE_ERROR_MESSAGE = "Error deleting file from S3: ";
    final SdkClientException sdkClientException = new SdkClientException("Error");
    given(s3.listObjects(bucketName, folderName)).willThrow(sdkClientException);

    final AppException thrownException =
        assertThrows(AppException.class, () -> sut.deleteFilesFromS3Bucket(objectToUploadImageUrl));

    assertEquals(
        DELETE_ERROR_MESSAGE + sdkClientException.getMessage(), thrownException.getMessage());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, thrownException.getStatus());
  }
}
