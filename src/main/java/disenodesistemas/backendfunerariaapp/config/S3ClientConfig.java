package disenodesistemas.backendfunerariaapp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3", matchIfMissing = true)
@EnableConfigurationProperties({AmazonS3Properties.class, S3BucketProperties.class})
public class S3ClientConfig {

  private final AmazonS3Properties amazonS3Properties;

  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
        .region(Region.of(amazonS3Properties.region()))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    amazonS3Properties.accessKey(), amazonS3Properties.secretKey())))
        .build();
  }
}
