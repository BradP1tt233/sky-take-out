package config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "sky.alioss")
public class AliOssProperties {
    @NotBlank
    private String endpoint;
    @NotBlank
    private String region;
    @NotBlank
    private String bucketName;
    @NotBlank
    private String accessKeyId;
    @NotBlank
    private String accessKeySecret;
}
