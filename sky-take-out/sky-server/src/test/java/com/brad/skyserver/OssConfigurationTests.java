package com.brad.skyserver;

import com.aliyun.oss.OSS;
import config.OssConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import utils.AliOssUtil;

import static org.assertj.core.api.Assertions.assertThat;

class OssConfigurationTests {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(OssConfiguration.class);

    @Test void disabledOssDoesNotRequireCredentials() {
        runner.withPropertyValues("sky.alioss.enabled=false").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(OSS.class).doesNotHaveBean(AliOssUtil.class);
        });
    }

    @Test void configuredOssRegistersReusableClientAndUploadTool() {
        runner.withPropertyValues(
                "sky.alioss.enabled=true",
                "sky.alioss.endpoint=https://oss-cn-beijing.aliyuncs.com",
                "sky.alioss.region=cn-beijing",
                "sky.alioss.bucket-name=test-bucket",
                "sky.alioss.access-key-id=test-key-id",
                "sky.alioss.access-key-secret=test-key-secret"
        ).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(OSS.class).hasSingleBean(AliOssUtil.class);
        });
    }
}
