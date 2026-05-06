package com.mall.api.controller;

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
 * Common file upload controller (C-end, requires login).
 */
@Api(tags = "文件上传")
@RestController
@RequestMapping("/api/v1/common/file")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class CommonFileController {

    private final FileService fileService;

    @ApiOperation("上传文件")
    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        String url = fileService.upload(file);
        return R.ok(url);
    }
}
