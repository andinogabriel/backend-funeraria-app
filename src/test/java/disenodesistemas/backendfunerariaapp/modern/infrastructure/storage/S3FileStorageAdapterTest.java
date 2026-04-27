package disenodesistemas.backendfunerariaapp.modern.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.model.FilePayload;
import disenodesistemas.backendfunerariaapp.config.S3BucketProperties;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.infrastructure.storage.S3FileStorageAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3FileStorageAdapter")
class S3FileStorageAdapterTest {

  @Mock private S3Client s3Client;

  @Test
  @DisplayName(
      "Given a valid image payload when the file is stored then it uploads the object to the configured bucket and returns the public URL")
  void givenAValidImagePayloadWhenTheFileIsStoredThenItUploadsTheObjectToTheConfiguredBucketAndReturnsThePublicUrl() {
    final S3FileStorageAdapter adapter =
        new S3FileStorageAdapter(
            s3Client, new S3BucketProperties("funeraria-bucket", "https://cdn.example.com/"));
    final FilePayload filePayload = new FilePayload("ataud.png", "image/png", "png-content".getBytes());
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenReturn(PutObjectResponse.builder().build());

    final String publicUrl = adapter.store(new ItemEntity(), filePayload);

    assertThat(publicUrl).startsWith("https://cdn.example.com/ItemEntity-");
    assertThat(publicUrl).endsWith("/ataud.png");
    verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  @DisplayName(
      "Given an item with a stored public URL when its files are deleted then the adapter resolves the relative prefix without deprecated string helpers")
  void givenAnItemWithAStoredPublicUrlWhenItsFilesAreDeletedThenTheAdapterResolvesTheRelativePrefixWithoutDeprecatedStringHelpers() {
    final S3FileStorageAdapter adapter =
        new S3FileStorageAdapter(
            s3Client, new S3BucketProperties("funeraria-bucket", "https://cdn.example.com/"));
    final ItemEntity itemEntity = new ItemEntity();
    itemEntity.setItemImageLink("https://cdn.example.com/items/item-123/ataud.png");
    when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
        .thenReturn(
            ListObjectsV2Response.builder()
                .contents(S3Object.builder().key("items/item-123/ataud.png").build())
                .build());

    adapter.deleteFiles(itemEntity);

    final ArgumentCaptor<ListObjectsV2Request> requestCaptor =
        ArgumentCaptor.forClass(ListObjectsV2Request.class);
    verify(s3Client).listObjectsV2(requestCaptor.capture());
    assertThat(requestCaptor.getValue().prefix()).isEqualTo("items/item-123");
  }

  @Test
  @DisplayName(
      "Given a bucket and object key when the file is downloaded then the adapter returns the stored bytes from S3")
  void givenABucketAndObjectKeyWhenTheFileIsDownloadedThenTheAdapterReturnsTheStoredBytesFromS3() {
    final S3FileStorageAdapter adapter =
        new S3FileStorageAdapter(
            s3Client, new S3BucketProperties("funeraria-bucket", "https://cdn.example.com/"));
    final ResponseBytes<GetObjectResponse> responseBytes =
        ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), "hello".getBytes());

    when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytes);

    assertThat(adapter.download("funeraria-bucket", "items/item-123/ataud.png"))
        .containsExactly("hello".getBytes());
  }
}
