package com.mall.admin.controller;

import com.mall.common.response.R;
import com.mall.model.dto.FloorSaveDTO;
import com.mall.model.entity.ContentFloor;
import com.mall.service.content.FloorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Platform floor management controller (admin).
 */
@Api(tags = "平台-楼层管理")
@RestController
@RequestMapping("/api/v1/platform/content/floor")
@RequiredArgsConstructor
public class PlatformFloorController {

    private final FloorService floorService;

    @ApiOperation("获取楼层列表")
    @GetMapping("/list")
    public R<List<ContentFloor>> list() {
        List<ContentFloor> list = floorService.listAll();
        return R.ok(list);
    }

    @ApiOperation("获取楼层详情")
    @GetMapping("/{id}")
    public R<ContentFloor> getById(@PathVariable Long id) {
        ContentFloor floor = floorService.getById(id);
        return R.ok(floor);
    }

    @ApiOperation("创建楼层")
    @PostMapping("/create")
    public R<Long> create(@Valid @RequestBody FloorSaveDTO dto) {
        Long id = floorService.create(dto);
        return R.ok(id);
    }

    @ApiOperation("更新楼层")
    @PutMapping("/update/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody FloorSaveDTO dto) {
        floorService.update(id, dto);
        return R.ok();
    }

    @ApiOperation("删除楼层")
    @DeleteMapping("/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        floorService.delete(id);
        return R.ok();
    }
}
