package com.brad.skyserver;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import utils.AliOssUtil;

import java.net.URI;
import java.time.Duration;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AliOssUtilTests {
    private final OSS client = mock(OSS.class);
    private final AliOssUtil util = new AliOssUtil(client, "test-bucket");

    @Test void uploadPreservesObjectKeyAndContentMetadata() throws Exception {
        byte[] bytes = {1, 2, 3};
        assertThat(util.upload(bytes, "dish/test.png", "image/png")).isEqualTo("dish/test.png");
        ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(client).putObject(request.capture());
        assertThat(request.getValue().getBucketName()).isEqualTo("test-bucket");
        assertThat(request.getValue().getKey()).isEqualTo("dish/test.png");
        assertThat(request.getValue().getMetadata().getContentType()).isEqualTo("image/png");
        assertThat(request.getValue().getMetadata().getContentLength()).isEqualTo(3);
        assertThat(request.getValue().getInputStream().readAllBytes()).containsExactly(bytes);
    }

    @Test void invalidUploadDoesNotCallOss() {
        assertThatIllegalArgumentException().isThrownBy(() -> util.upload(new byte[0], "dish/test.png"));
        assertThatIllegalArgumentException().isThrownBy(() -> util.upload(new byte[]{1}, " "));
        verifyNoInteractions(client);
    }

    @Test void failedUploadPropagatesSdkError() {
        ClientException failure = new ClientException("upload failed");
        when(client.putObject(any(PutObjectRequest.class))).thenThrow(failure);
        assertThatThrownBy(() -> util.upload(new byte[]{1}, "dish/test.png")).isSameAs(failure);
    }

    @Test void privateObjectUsesAnExpiringSignedUrl() throws Exception {
        var expected = URI.create("https://test-bucket.example.com/dish/test.png?signature=test").toURL();
        when(client.generatePresignedUrl(eq("test-bucket"), eq("dish/test.png"), any(Date.class)))
                .thenReturn(expected);
        long before = System.currentTimeMillis();
        assertThat(util.generatePresignedUrl("dish/test.png", Duration.ofMinutes(15))).isEqualTo(expected);
        ArgumentCaptor<Date> expiry = ArgumentCaptor.forClass(Date.class);
        verify(client).generatePresignedUrl(eq("test-bucket"), eq("dish/test.png"), expiry.capture());
        assertThat(expiry.getValue().getTime()).isBetween(before + 899_000, System.currentTimeMillis() + 901_000);
    }

    @Test void invalidExpiryDoesNotCallOss() {
        assertThatIllegalArgumentException().isThrownBy(() -> util.generatePresignedUrl("a", Duration.ZERO));
        assertThatIllegalArgumentException().isThrownBy(() -> util.generatePresignedUrl("a", Duration.ofDays(8)));
        verifyNoInteractions(client);
    }
}
