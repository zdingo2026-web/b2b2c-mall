package com.mall.admin.controller;

import com.mall.common.response.R;
import com.mall.model.vo.PlatformOverviewVO;
import com.mall.service.system.StatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Platform statistics controller (admin).
 */
@Api(tags = "平台-数据看板")
@RestController
@RequestMapping("/api/v1/platform/statistics")
@RequiredArgsConstructor
public class PlatformStatisticsController {

    private final StatisticsService statisticsService;

    @ApiOperation("获取平台概览")
    @GetMapping("/overview")
    public R<PlatformOverviewVO> getPlatformOverview() {
        PlatformOverviewVO vo = statisticsService.getPlatformOverview();
        return R.ok(vo);
    }
}
