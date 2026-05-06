package com.mall.admin.controller;

import com.mall.common.response.R;
import com.mall.model.dto.ConfigUpdateDTO;
import com.mall.model.vo.ConfigVO;
import com.mall.service.system.SysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Platform system config controller (admin).
 */
@Api(tags = "平台-系统配置")
@RestController
@RequestMapping("/api/v1/platform/config")
@RequiredArgsConstructor
public class PlatformConfigController {

    private final SysConfigService sysConfigService;

    @ApiOperation("获取配置列表")
    @GetMapping("/list")
    public R<List<ConfigVO>> getConfigList(
            @RequestParam(required = false) String configGroup) {
        List<ConfigVO> list = sysConfigService.getConfigList(configGroup);
        return R.ok(list);
    }

    @ApiOperation("更新配置")
    @PutMapping("/update")
    public R<Void> updateConfig(@Valid @RequestBody ConfigUpdateDTO dto) {
        sysConfigService.updateConfig(dto);
        return R.ok();
    }
}
