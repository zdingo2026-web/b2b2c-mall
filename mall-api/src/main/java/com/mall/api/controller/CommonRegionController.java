package com.mall.api.controller;

import com.mall.common.response.R;
import com.mall.model.vo.RegionVO;
import com.mall.service.system.RegionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Common region controller (C-end, public).
 */
@Api(tags = "地区")
@RestController
@RequestMapping("/api/v1/common/region")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class CommonRegionController {

    private final RegionService regionService;

    @ApiOperation("获取地区树")
    @GetMapping("/tree")
    public R<List<RegionVO>> getRegionTree() {
        List<RegionVO> tree = regionService.getRegionTree();
        return R.ok(tree);
    }

    @ApiOperation("获取子级地区")
    @GetMapping("/{id}/children")
    public R<List<RegionVO>> getChildren(@PathVariable Long id) {
        List<RegionVO> children = regionService.getChildren(id);
        return R.ok(children);
    }
}
