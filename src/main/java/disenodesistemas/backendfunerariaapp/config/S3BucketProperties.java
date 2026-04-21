package disenodesistemas.backendfunerariaapp.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "aws.s3.bucket")
public record S3BucketProperties(@NotBlank String name, @NotBlank String url) {}
