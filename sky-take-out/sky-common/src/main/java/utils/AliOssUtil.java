package utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/** OSS 上传工具。上传返回稳定的对象 key；签名链接仅用于临时展示。 */
public final class AliOssUtil {
    private final OSS ossClient;
    private final String bucketName;

    public AliOssUtil(OSS ossClient, String bucketName) {
        this.ossClient = Objects.requireNonNull(ossClient, "OSS client must not be null");
        this.bucketName = requireText(bucketName, "Bucket name");
    }

    public String upload(byte[] bytes, String objectName) {
        return upload(bytes, objectName, "application/octet-stream");
    }

    public String upload(byte[] bytes, String objectName, String contentType) {
        Objects.requireNonNull(bytes, "File bytes must not be null");
        return upload(new ByteArrayInputStream(bytes), objectName, contentType, bytes.length);
    }

    /** 调用方负责关闭输入流。上传失败时保留 SDK 异常，不返回成功结果。 */
    public String upload(InputStream inputStream, String objectName,
                         String contentType, long contentLength) {
        Objects.requireNonNull(inputStream, "Input stream must not be null");
        requireText(objectName, "Object name");
        requireText(contentType, "Content type");
        if (contentLength <= 0) {
            throw new IllegalArgumentException("File must not be empty");
        }
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(contentLength);
        ossClient.putObject(new PutObjectRequest(bucketName, objectName, inputStream, metadata));
        return objectName;
    }

    public URL generatePresignedUrl(String objectName) {
        return generatePresignedUrl(objectName, Duration.ofMinutes(15));
    }

    /** 此链接会过期，数据库应保存 objectName，而非签名链接。 */
    public URL generatePresignedUrl(String objectName, Duration validity) {
        requireText(objectName, "Object name");
        Objects.requireNonNull(validity, "Validity must not be null");
        if (validity.isNegative() || validity.isZero() || validity.compareTo(Duration.ofDays(7)) > 0) {
            throw new IllegalArgumentException("URL validity must be positive and no more than 7 days");
        }
        return ossClient.generatePresignedUrl(bucketName, objectName,
                Date.from(Instant.now().plus(validity)));
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
