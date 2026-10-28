package com.brad.skyserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.*;

class ProfileConfigurationTests {
    // 隔离真实环境变量，验证配置加载时不访问数据库或 OSS。
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withInitializer(context -> {
                context.getEnvironment().getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
                context.getEnvironment().getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
            })
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withPropertyValues(
                    "DB_URL=jdbc:mysql://localhost:3306/dev_test",
                    "DB_USERNAME=dev-user",
                    "DB_PASSWORD=dev-test-password",
                    "SKY_JWT_ADMIN_SECRET_KEY=dev-test-secret",
                    "OSS_ACCESS_KEY_ID=dev-test-key",
                    "OSS_ACCESS_KEY_SECRET=dev-test-secret");

    @Test void defaultProfileLoadsDevelopmentConfigurationAndCommonSettings() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            var env = context.getEnvironment();
            assertThat(env.getDefaultProfiles()).containsExactly("dev");
            assertThat(env.getProperty("spring.datasource.url")).isEqualTo("jdbc:mysql://localhost:3306/dev_test");
            assertThat(env.getProperty("sky.alioss.bucket-name")).isEqualTo("skytakeout-brad");
            assertThat(env.getProperty("sky.jwt.admin-secret-key")).isEqualTo("dev-test-secret");
            assertThat(env.getProperty("spring.datasource.driver-class-name")).isEqualTo("com.mysql.cj.jdbc.Driver");
        });
    }

    @Test void productionUsesOnlyProductionConnectionAndCredentials() {
        runner.withPropertyValues(
                "spring.profiles.active=prod",
                "PROD_DB_URL=jdbc:mysql://prod-db:3306/prod_test",
                "PROD_DB_USERNAME=prod-user",
                "PROD_DB_PASSWORD=prod-test-password",
                "PROD_SKY_JWT_ADMIN_SECRET_KEY=prod-test-secret",
                "PROD_OSS_ENDPOINT=https://oss-cn-beijing.aliyuncs.com",
                "PROD_OSS_REGION=cn-beijing",
                "PROD_OSS_BUCKET_NAME=prod-test-bucket",
                "PROD_OSS_ACCESS_KEY_ID=prod-test-key",
                "PROD_OSS_ACCESS_KEY_SECRET=prod-test-secret"
        ).run(context -> {
            assertThat(context).hasNotFailed();
            var env = context.getEnvironment();
            assertThat(env.getActiveProfiles()).containsExactly("prod");
            assertThat(env.getProperty("spring.datasource.url")).isEqualTo("jdbc:mysql://prod-db:3306/prod_test");
            assertThat(env.getProperty("spring.datasource.username")).isEqualTo("prod-user");
            assertThat(env.getProperty("spring.datasource.password")).isEqualTo("prod-test-password");
            assertThat(env.getProperty("sky.jwt.admin-secret-key")).isEqualTo("prod-test-secret");
            assertThat(env.getProperty("sky.alioss.bucket-name")).isEqualTo("prod-test-bucket");
            assertThat(env.getProperty("sky.alioss.access-key-id")).isEqualTo("prod-test-key");
            assertThat(env.getProperty("sky.alioss.access-key-secret")).isEqualTo("prod-test-secret");
        });
    }

    @Test void missingProductionVariablesNeverFallBackToDevelopment() {
        runner.withPropertyValues("spring.profiles.active=prod").run(context -> {
            var env = context.getEnvironment();
            assertThatThrownBy(() -> env.getProperty("spring.datasource.url"))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("PROD_DB_URL");
            assertThatThrownBy(() -> env.getProperty("sky.jwt.admin-secret-key"))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("PROD_SKY_JWT_ADMIN_SECRET_KEY");
            assertThat(env.getProperty("sky.alioss.bucket-name")).isEmpty();
            assertThat(env.getProperty("sky.alioss.access-key-id")).isEmpty();
        });
    }
}
