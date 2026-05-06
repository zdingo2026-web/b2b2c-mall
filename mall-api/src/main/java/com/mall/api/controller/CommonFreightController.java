package com.mall.api.controller;

import com.mall.common.response.R;
import com.mall.model.dto.FreightCalculateDTO;
import com.mall.model.vo.FreightVO;
import com.mall.service.system.FreightService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Common freight controller (C-end, public).
 */
@Api(tags = "运费计算")
@RestController
@RequestMapping("/api/v1/common/freight")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class CommonFreightController {

    private final FreightService freightService;

    @ApiOperation("计算运费")
    @PostMapping("/calculate")
    public R<FreightVO> calculateFreight(@Valid @RequestBody FreightCalculateDTO dto) {
        FreightVO result = freightService.calculateFreight(dto);
        return R.ok(result);
    }
}
