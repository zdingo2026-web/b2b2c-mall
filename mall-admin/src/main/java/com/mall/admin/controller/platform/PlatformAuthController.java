package com.mall.admin.controller.platform;

import com.mall.common.constant.RedisKeyConstant;
import com.mall.common.response.R;
import com.mall.common.util.RateLimitUtil;
import com.mall.model.vo.LoginVO;
import com.mall.service.user.AuthService;
import com.mall.service.user.SysAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Platform admin authentication controller.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/platform/auth")
@RequiredArgsConstructor
public class PlatformAuthController {

    private final SysAdminService sysAdminService;
    private final AuthService authService;
    private final RateLimitUtil rateLimitUtil;

    @PostMapping("/login")
    public R<LoginVO> login(@RequestParam String username, @RequestParam String password,
                            HttpServletRequest request) {
        String ip = getClientIp(request);
        String rateLimitKey = RedisKeyConstant.RATE_LIMIT + "login:" + ip + ":" + username;
        rateLimitUtil.checkRateLimit(rateLimitKey, 5, 300);
        LoginVO result = sysAdminService.login(username, password);
        rateLimitUtil.clearRateLimit(rateLimitKey);
        return R.ok(result);
    }

    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest request) {
        String token = extractToken(request);
        authService.logout(token);
        return R.ok();
    }

    @GetMapping("/info")
    public R<LoginVO.UserInfo> info() {
        return R.ok(sysAdminService.getInfo());
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
            // X-Forwarded-For may contain multiple IPs, use the first one
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
