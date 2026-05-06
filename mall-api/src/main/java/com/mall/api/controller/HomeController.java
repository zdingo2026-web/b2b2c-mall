package com.mall.api.controller;

import com.mall.common.response.R;
import com.mall.model.vo.HomeVO;
import com.mall.service.content.HomeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Home page controller (C-end, public).
 */
@Api(tags = "首页")
@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @ApiOperation("获取首页聚合数据")
    @GetMapping("/home")
    public R<HomeVO> getHomeData() {
        HomeVO homeVO = homeService.getHomeData();
        return R.ok(homeVO);
    }
}
