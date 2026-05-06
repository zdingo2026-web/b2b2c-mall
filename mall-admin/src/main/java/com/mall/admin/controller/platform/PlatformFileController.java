package com.mall.admin.controller.platform;

import com.mall.common.response.R;
import com.mall.service.content.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Platform file upload controller.
 */
@Api(tags = "平台-文件上传")
@RestController
@RequestMapping("/api/v1/platform/file")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM')")
public class PlatformFileController {

    private final FileService fileService;

    @ApiOperation("上传文件")
    @PostMapping("/upload")
    @PreAuthorize("hasRole('PLATFORM')")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        String url = fileService.upload(file);
        return R.ok(url);
    }
}
