# 开发和生产环境配置

- application.yml：公共配置，包括默认环境 dev、数据库驱动、端口、MyBatis 和 JWT 有效期。
- application-dev.yml：开发环境，继续使用原有环境变量。
- application-prod.yml：生产环境，使用独立的 PROD_ 环境变量。

## 切换环境

IDEA：运行 → 编辑配置 → SkyServerApplication → 环境变量，添加 SPRING_PROFILES_ACTIVE=dev 或 SPRING_PROFILES_ACTIVE=prod。未指定时默认使用 dev。

也可以使用程序参数 --spring.profiles.active=prod；它不是 VM 选项。只选择一个环境，不要同时激活 dev 和 prod。

CMD 当前窗口切换：

```cmd
set SPRING_PROFILES_ACTIVE=dev
```

生产部署启动示例（先配置生产环境变量）：

```cmd
java -jar sky-server.jar --spring.profiles.active=prod
```

文件名应替换为实际构建的 JAR。set 只影响当前 CMD 及其启动的子进程；setx 设置只对之后启动的进程生效，修改后需要重新打开 IDEA。

## 环境变量对照

| 配置 | dev | prod |
| --- | --- | --- |
| 数据库 URL | DB_URL | PROD_DB_URL |
| 数据库用户名 | DB_USERNAME | PROD_DB_USERNAME |
| 数据库密码 | DB_PASSWORD | PROD_DB_PASSWORD |
| JWT 密钥 | SKY_JWT_ADMIN_SECRET_KEY | PROD_SKY_JWT_ADMIN_SECRET_KEY |
| OSS 开关 | OSS_ENABLED，默认 true | PROD_OSS_ENABLED，默认 true |
| OSS Endpoint | OSS_ENDPOINT，默认北京 Endpoint | PROD_OSS_ENDPOINT |
| OSS Region | OSS_REGION，默认 cn-beijing | PROD_OSS_REGION |
| OSS Bucket | OSS_BUCKET_NAME，默认 skytakeout-brad | PROD_OSS_BUCKET_NAME |
| OSS AccessKey ID | OSS_ACCESS_KEY_ID | PROD_OSS_ACCESS_KEY_ID |
| OSS AccessKey Secret | OSS_ACCESS_KEY_SECRET | PROD_OSS_ACCESS_KEY_SECRET |

SERVER_PORT 可用于两个环境，默认 8080。生产数据库和 JWT 变量必须提供；启用 OSS 时还必须提供生产 OSS 配置。生产配置不回退到开发环境变量。开发和生产环境应使用各自的数据库、Bucket 及凭证。所有密码和密钥仍由环境变量提供，不填写到 YAML 中。

本次没有生成或设置生产环境凭证；请在部署环境中配置实际值。

Spring Boot Profiles 官方说明：https://docs.spring.io/spring-boot/reference/features/profiles.html
