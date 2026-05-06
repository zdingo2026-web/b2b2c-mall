package com.mall.service.content;

import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.common.util.TenantContext;
import com.mall.dao.mapper.SysFileMapper;
import com.mall.model.entity.SysFile;
import com.mall.service.config.MinioConfig;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final SysFileMapper sysFileMapper;
    private final MinioConfig minioConfig;
    private final io.minio.MinioClient minioClient;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private static final Map<String, String> EXTENSION_CONTENT_TYPE;
    static {
        Map<String, String> m = new HashMap<>();
        m.put("jpg", "image/jpeg");
        m.put("jpeg", "image/jpeg");
        m.put("png", "image/png");
        m.put("gif", "image/gif");
        m.put("webp", "image/webp");
        EXTENSION_CONTENT_TYPE = Collections.unmodifiableMap(m);
    }

    public String upload(MultipartFile file) {
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "上传文件不能为空");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAIL.getCode(), "文件大小不能超过5MB");
        }

        // Validate file type by extension
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (extension.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件名缺少扩展名");
        }
        if (!ALLOWED_IMAGE_TYPES.contains(extension.toLowerCase())) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAIL.getCode(), "不支持的文件类型，仅支持: jpg, jpeg, png, gif, webp");
        }

        // Generate file path: date directory + UUID filename
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String relativePath = dateDir + "/" + newFilename;

        // Derive content type from validated extension (never trust client-provided Content-Type)
        String contentType = EXTENSION_CONTENT_TYPE.getOrDefault(extension.toLowerCase(), "application/octet-stream");

        // Upload to MinIO with magic byte validation
        try (InputStream rawInputStream = file.getInputStream();
             InputStream inputStream = new BufferedInputStream(rawInputStream)) {

            // Validate file content matches its declared extension via magic bytes
            inputStream.mark(12);
            byte[] header = new byte[12];
            int bytesRead = inputStream.read(header);
            inputStream.reset();
            if (bytesRead >= 4 && !isValidMagicBytes(header, extension.toLowerCase())) {
                throw new BusinessException(ResultCode.FILE_UPLOAD_FAIL.getCode(), "文件内容与类型不匹配");
            }
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(relativePath)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            log.error("[FileService] Failed to upload file to MinIO: {}", relativePath, e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAIL.getCode(), "文件上传失败");
        }

        // Build access URL
        String url = minioConfig.getUrlPrefix() + relativePath;

        // Record to SysFile
        SysFile sysFile = new SysFile();
        sysFile.setFileName(originalFilename);
        sysFile.setFilePath(relativePath);
        sysFile.setFileSize(file.getSize());
        sysFile.setFileType(extension);
        sysFile.setUrl(url);
        sysFile.setTenantId(TenantContext.getTenantId());
        sysFileMapper.insert(sysFile);

        log.info("[FileService] File uploaded: original={}, url={}", originalFilename, url);
        return url;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Validate file content against known magic bytes for the given extension.
     * Returns true for extensions without known magic byte patterns.
     */
    private boolean isValidMagicBytes(byte[] header, String extension) {
        switch (extension) {
            case "jpg":
            case "jpeg":
                return header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF;
            case "png":
                return header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47;
            case "gif":
                return header[0] == 0x47 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x38;
            case "webp":
                return header.length >= 12 && header[8] == 0x57 && header[9] == 0x45 && header[10] == 0x42 && header[11] == 0x50;
            default:
                // No known magic byte pattern for this extension; allow by default
                return true;
        }
    }
}
