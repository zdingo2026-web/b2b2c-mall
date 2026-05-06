package com.mall.api.controller;

import com.mall.common.response.R;
import com.mall.service.content.SmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

/**
 * Common SMS controller (C-end, public).
 */
@Api(tags = "短信验证码")
@RestController
@RequestMapping("/api/v1/common/sms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class CommonSmsController {

    private final SmsService smsService;

    @ApiOperation("发送验证码")
    @PostMapping("/send")
    public R<Void> sendCode(@RequestParam @NotBlank(message = "手机号不能为空") String phone) {
        smsService.sendCode(phone);
        return R.ok();
    }
}
