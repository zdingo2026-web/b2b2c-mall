package com.mall.admin.controller.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.constant.RedisKeyConstant;
import com.mall.common.enums.UserTypeEnum;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.R;
import com.mall.common.response.ResultCode;
import com.mall.common.util.JwtUtil;
import com.mall.common.util.PasswordUtil;
import com.mall.common.util.RateLimitUtil;
import com.mall.dao.mapper.TenantAdminMapper;
import com.mall.model.entity.TenantAdmin;
import com.mall.model.vo.LoginVO;
import com.mall.service.user.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

/**
 * Merchant admin authentication controller.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/merchant/auth")
@RequiredArgsConstructor
public class MerchantAuthController {

    private final TenantAdminMapper tenantAdminMapper;
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final RateLimitUtil rateLimitUtil;

    @PostMapping("/login")
    public R<LoginVO> login(@RequestParam String username, @RequestParam String password,
                            HttpServletRequest request) {
        String ip = getClientIp(request);
        String rateLimitKey = RedisKeyConstant.RATE_LIMIT + "login:" + ip + ":" + username;
        rateLimitUtil.checkRateLimit(rateLimitKey, 5, 300);
        TenantAdmin admin = tenantAdminMapper.selectOne(
                new LambdaQueryWrapper<TenantAdmin>().eq(TenantAdmin::getUsername, username));
        if (admin == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (admin.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        if (!PasswordUtil.verify(password, admin.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // Update last login time
        admin.setLastLoginTime(LocalDateTime.now());
        tenantAdminMapper.updateById(admin);

        String accessToken = jwtUtil.generateToken(admin.getId(), UserTypeEnum.MERCHANT.getCode(), admin.getTenantId());
        String refreshToken = jwtUtil.generateRefreshToken(admin.getId(), UserTypeEnum.MERCHANT.getCode(), admin.getTenantId());

        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(accessToken);
        loginVO.setRefreshToken(refreshToken);

        LoginVO.UserInfo userInfo = new LoginVO.UserInfo();
        userInfo.setUserId(admin.getId());
        userInfo.setUsername(admin.getUsername());
        userInfo.setNickname(admin.getRealName());
        userInfo.setUserType(UserTypeEnum.MERCHANT.getCode());
        userInfo.setTenantId(admin.getTenantId());
        loginVO.setUserInfo(userInfo);

        rateLimitUtil.clearRateLimit(rateLimitKey);
        return R.ok(loginVO);
    }

    @GetMapping("/info")
    public R<LoginVO.UserInfo> info() {
        Long userId = com.mall.common.util.UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(com.mall.common.response.ResultCode.UNAUTHORIZED);
        }

        TenantAdmin admin = tenantAdminMapper.selectById(userId);
        if (admin == null) {
            throw new BusinessException(com.mall.common.response.ResultCode.USER_NOT_FOUND);
        }

        LoginVO.UserInfo userInfo = new LoginVO.UserInfo();
        userInfo.setUserId(admin.getId());
        userInfo.setUsername(admin.getUsername());
        userInfo.setNickname(admin.getRealName());
        userInfo.setAvatar("");
        userInfo.setUserType(UserTypeEnum.MERCHANT.getCode());
        userInfo.setTenantId(admin.getTenantId());
        return R.ok(userInfo);
    }

    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest request) {
        String token = extractToken(request);
        authService.logout(token);
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
