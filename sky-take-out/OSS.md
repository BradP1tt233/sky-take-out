# OSS 文件上传

上传工具使用阿里云 OSS Java SDK V1 3.18.4 和 V4 签名，通过 Spring 注入 utils.AliOssUtil。

## 环境变量

以下变量适用于默认 dev 环境；prod 环境使用对应的 PROD_ 变量，详见 [环境配置说明](PROFILES.md)。

凭证仅保存在当前 Windows 用户环境变量中，不写入代码、YAML 或提交记录。CMD 的 setx 设置只对新启动的进程生效；设置或轮换后需要完全退出并重新打开 IDEA。不要勾选共享包含凭据的运行配置。

| 变量 | 说明 |
| --- | --- |
| OSS_ACCESS_KEY_ID | RAM 身份 AccessKey ID，必填 |
| OSS_ACCESS_KEY_SECRET | AccessKey Secret，必填 |
| OSS_ENDPOINT | 默认 https://oss-cn-beijing.aliyuncs.com |
| OSS_REGION | 默认 cn-beijing（不是 oss-cn-beijing） |
| OSS_BUCKET_NAME | 默认 skytakeout-brad |
| OSS_ENABLED | 默认 true；不使用 OSS 时设为 false |

启用 OSS 而未提供凭证时，应用配置校验失败；请先设置环境变量。RAM 身份至少需要目标 Bucket 对象上传权限；展示私有图片还需要读取权限。工具不会修改 Bucket ACL 或将文件设为公共读取。

## 调用示例

在 Spring 管理的服务类中通过构造器注入 AliOssUtil，然后使用：

```java
String objectName = "dish/" + java.util.UUID.randomUUID() + ".jpg";
String objectKey;
try (var input = file.getInputStream()) {
    objectKey = aliOssUtil.upload(input, objectName, "image/jpeg", file.getSize());
}
String previewUrl = aliOssUtil.generatePresignedUrl(objectKey).toString();
```

示例假定 file 为已校验的 JPEG MultipartFile。HTTP 接口层应先检查登录权限、文件大小和实际图片内容，并根据实际格式设置扩展名及 Content-Type。也支持 upload(byte[], objectName, contentType)。

上传返回的是对象 key，适合保存到菜品 image 字段；私有图片通过 generatePresignedUrl 生成默认 15 分钟的临时访问地址，展示时重新生成，不将临时地址永久保存到数据库。上传失败时直接抛出 SDK 异常，交给接口层统一处理。输入流由调用方关闭，OSS 客户端随 Spring 容器关闭而释放。

本次只提供配置和工具类，尚未添加 HTTP 上传接口。

## 验证

以下测试使用模拟 OSS 客户端，不上传真实文件，也不修改数据库：

```powershell
mvn -B -pl sky-server -am '-Dtest=AliOssUtilTests,OssConfigurationTests,BaseContextFilterTests,EmployeeLoginTests' '-Dsurefire.failIfNoSpecifiedTests=false' '-Dsky.alioss.enabled=false' test
```

官方 SDK 文档：https://help.aliyun.com/zh/oss/developer-reference/oss-java-sdk/
