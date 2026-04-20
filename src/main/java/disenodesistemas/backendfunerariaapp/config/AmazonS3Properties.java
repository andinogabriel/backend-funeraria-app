package disenodesistemas.backendfunerariaapp.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "amazon-s3")
public record AmazonS3Properties(
    @NotBlank String accessKey,
    @NotBlank String secretKey,
    @DefaultValue("sa-east-1") @NotBlank String region) {}
