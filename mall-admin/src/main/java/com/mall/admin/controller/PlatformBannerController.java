package com.mall.admin.controller;

import com.mall.common.response.R;
import com.mall.model.dto.BannerSaveDTO;
import com.mall.model.entity.ContentBanner;
import com.mall.service.content.BannerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Platform banner management controller (admin).
 */
@Api(tags = "平台-Banner管理")
@RestController
@RequestMapping("/api/v1/platform/content/banner")
@RequiredArgsConstructor
public class PlatformBannerController {

    private final BannerService bannerService;

    @ApiOperation("获取Banner列表")
    @GetMapping("/list")
    public R<List<ContentBanner>> list() {
        List<ContentBanner> list = bannerService.listAll();
        return R.ok(list);
    }

    @ApiOperation("获取Banner详情")
    @GetMapping("/{id}")
    public R<ContentBanner> getById(@PathVariable Long id) {
        ContentBanner banner = bannerService.getById(id);
        return R.ok(banner);
    }

    @ApiOperation("创建Banner")
    @PostMapping("/create")
    public R<Long> create(@Valid @RequestBody BannerSaveDTO dto) {
        Long id = bannerService.create(dto);
        return R.ok(id);
    }

    @ApiOperation("更新Banner")
    @PutMapping("/update/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody BannerSaveDTO dto) {
        bannerService.update(id, dto);
        return R.ok();
    }

    @ApiOperation("删除Banner")
    @DeleteMapping("/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return R.ok();
    }
}
