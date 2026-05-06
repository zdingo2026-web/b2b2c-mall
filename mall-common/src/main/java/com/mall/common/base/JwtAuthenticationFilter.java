package com.mall.common.base;

import com.mall.common.constant.CommonConstant;
import com.mall.common.constant.RedisKeyConstant;
import com.mall.common.enums.UserTypeEnum;
import com.mall.common.util.JwtUtil;
import com.mall.common.util.TenantContext;
import com.mall.common.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT authentication filter.
 * Extracts JWT token from Authorization header, validates it,
 * checks token blacklist, and sets Spring Security context + TenantContext.
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, StringRedisTemplate stringRedisTemplate) {
        this.jwtUtil = jwtUtil;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);

            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                // C-02 fix: Check token blacklist before accepting
                String blackKey = RedisKeyConstant.TOKEN_BLACKLIST + token;
                if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(blackKey))) {
                    log.debug("Token is blacklisted, skipping authentication");
                    filterChain.doFilter(request, response);
                    return;
                }

                Long userId = jwtUtil.getUserId(token);
                Integer userType = jwtUtil.getUserType(token);
                Long tenantId = jwtUtil.getTenantId(token);

                // Set tenant context for MyBatis-Plus interceptor
                TenantContext.setTenantId(tenantId);

                // Set security context holder
                UserContext.setUserId(userId);
                UserContext.setUserType(userType);
                UserContext.setTenantId(tenantId);

                // Set Spring Security authentication
                String roleName = getUserRoleName(userType);
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, Collections.singletonList(authority));
                authentication.setDetails(userType);
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } finally {
            // Clean up ThreadLocal to prevent memory leaks
            TenantContext.clear();
            UserContext.clear();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(CommonConstant.TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(CommonConstant.TOKEN_PREFIX)) {
            return bearerToken.substring(CommonConstant.TOKEN_PREFIX.length());
        }
        return null;
    }

    private String getUserRoleName(Integer userType) {
        if (userType == null) {
            return "ROLE_UNKNOWN";
        }
        UserTypeEnum typeEnum = UserTypeEnum.fromCode(userType);
        switch (typeEnum) {
            case PLATFORM:
                return "ROLE_PLATFORM";
            case MERCHANT:
                return "ROLE_MERCHANT";
            case MEMBER:
                return "ROLE_MEMBER";
            default:
                return "ROLE_UNKNOWN";
        }
    }
}
