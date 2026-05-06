package com.mall.api.controller.member;

import com.mall.common.constant.RedisKeyConstant;
import com.mall.common.response.R;
import com.mall.common.util.RateLimitUtil;
import com.mall.common.util.UserContext;
import com.mall.model.dto.MemberLoginDTO;
import com.mall.model.dto.MemberRegisterDTO;
import com.mall.model.dto.PhoneLoginDTO;
import com.mall.model.dto.WechatLoginDTO;
import com.mall.model.vo.LoginVO;
import com.mall.service.sms.SmsCodeService;
import com.mall.service.user.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * Member authentication controller (C-end).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/member/auth")
@RequiredArgsConstructor
public class MemberAuthController {

    private final AuthService authService;
    private final SmsCodeService smsCodeService;
    private final RateLimitUtil rateLimitUtil;

    @PostMapping("/register")
    public R<LoginVO> register(@Validated @RequestBody MemberRegisterDTO dto) {
        return R.ok(authService.register(dto));
    }

    @PostMapping("/login")
    public R<LoginVO> login(@Validated @RequestBody MemberLoginDTO dto,
                            HttpServletRequest request) {
        String ip = getClientIp(request);
        String rateLimitKey = RedisKeyConstant.RATE_LIMIT + "login:" + ip + ":" + dto.getUsername();
        rateLimitUtil.checkRateLimit(rateLimitKey, 5, 300);
        LoginVO result = authService.login(dto);
        rateLimitUtil.clearRateLimit(rateLimitKey);
        return R.ok(result);
    }

    @PostMapping("/login/phone")
    public R<LoginVO> phoneLogin(@Validated @RequestBody PhoneLoginDTO dto,
                                 HttpServletRequest request) {
        String ip = getClientIp(request);
        String rateLimitKey = RedisKeyConstant.RATE_LIMIT + "login:" + ip + ":" + dto.getPhone();
        rateLimitUtil.checkRateLimit(rateLimitKey, 5, 300);
        LoginVO result = authService.phoneLogin(dto);
        rateLimitUtil.clearRateLimit(rateLimitKey);
        return R.ok(result);
    }

    @PostMapping("/wechat")
    public R<LoginVO> wechatLogin(@Validated @RequestBody WechatLoginDTO dto) {
        return R.ok(authService.wechatLogin(dto));
    }

    @PostMapping("/refresh")
    public R<LoginVO> refresh(@RequestParam String refreshToken) {
        return R.ok(authService.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest request) {
        String token = extractToken(request);
        authService.logout(token);
        return R.ok();
    }

    /**
     * Send SMS verification code.
     * bizType: 1-login, 2-register, 3-change password, 4-bind phone
     */
    @PostMapping("/sms/send")
    public R<Void> sendSmsCode(
            @NotBlank(message = "手机号不能为空")
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
            @RequestParam String phone,
            @RequestParam(defaultValue = "1") Integer bizType,
            HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        smsCodeService.sendCode(phone, bizType, ip);
        return R.ok();
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
