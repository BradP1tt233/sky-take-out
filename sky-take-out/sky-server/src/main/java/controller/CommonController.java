package controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import result.Result;
import utils.AliOssUtil;

import javax.management.ObjectName;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;
    @PostMapping("upload")
    public Result<String> upload(MultipartFile file) {
        log.info("上传文件：{}", file);
        try (InputStream input = file.getInputStream();) {

            String objectName = "dish/" + UUID.randomUUID() + ".jpg";
            String objectKey = aliOssUtil.upload(input, objectName, "image/jpeg", file.getSize());
            String fileUrl = aliOssUtil.generatePresignedUrl(objectKey).toString();
            return Result.success(fileUrl);
        } catch (IOException e) {
           log.error("上传文件失败");
           return Result.error("上传失败");
        }
    }
}
