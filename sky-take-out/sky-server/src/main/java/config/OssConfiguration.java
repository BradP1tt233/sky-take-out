package config;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import utils.AliOssUtil;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "sky.alioss", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AliOssProperties.class)
public class OssConfiguration {
    @Bean(destroyMethod = "shutdown")
    public OSS ossClient(AliOssProperties properties) {
        ClientBuilderConfiguration clientConfiguration = new ClientBuilderConfiguration();
        clientConfiguration.setSignatureVersion(SignVersion.V4);
        return OSSClientBuilder.create()
                .endpoint(properties.getEndpoint())
                .region(properties.getRegion())
                .credentialsProvider(new DefaultCredentialProvider(
                        properties.getAccessKeyId(), properties.getAccessKeySecret()))
                .clientConfiguration(clientConfiguration)
                .build();
    }

    @Bean
    public AliOssUtil aliOssUtil(OSS ossClient, AliOssProperties properties) {
        return new AliOssUtil(ossClient, properties.getBucketName());
    }
}
