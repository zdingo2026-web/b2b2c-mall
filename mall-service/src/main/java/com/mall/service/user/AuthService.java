package com.mall.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.constant.RedisKeyConstant;
import com.mall.common.enums.UserTypeEnum;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.common.util.JwtUtil;
import com.mall.common.util.PasswordUtil;
import com.mall.dao.mapper.MemberAuthMapper;
import com.mall.dao.mapper.MemberMapper;
import com.mall.model.dto.MemberLoginDTO;
import com.mall.model.dto.MemberRegisterDTO;
import com.mall.model.dto.PhoneLoginDTO;
import com.mall.model.dto.WechatLoginDTO;
import com.mall.model.entity.Member;
import com.mall.model.entity.MemberAuth;
import com.mall.model.vo.LoginVO;
import com.mall.service.sms.SmsCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Member authentication service.
 * Handles register, login, wechat login, token refresh, and logout.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberMapper memberMapper;
    private final MemberAuthMapper memberAuthMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final SmsCodeService smsCodeService;

    /**
     * Register a new member.
     * Validates phone uniqueness, SMS code, and password strength.
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(MemberRegisterDTO dto) {
        // Check if phone already registered
        Long count = memberMapper.selectCount(
                new LambdaQueryWrapper<Member>().eq(Member::getPhone, dto.getPhone()));
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_PHONE_EXISTS);
        }

        // Verify SMS code via database-backed service
        boolean valid = smsCodeService.verifyCode(dto.getPhone(), dto.getCode(), 2);
        if (!valid) {
            throw new BusinessException("验证码错误或已过期");
        }

        // Create member
        Member member = new Member();
        member.setPhone(dto.getPhone());
        member.setUsername(dto.getPhone());
        member.setPassword(PasswordUtil.encode(dto.getPassword()));
        member.setNickname("用户" + dto.getPhone().substring(7));
        member.setStatus(1);
        member.setLastLoginTime(LocalDateTime.now());
        memberMapper.insert(member);

        // Generate token and return
        return buildLoginVO(member.getId(), member.getUsername(), member.getNickname(),
                member.getAvatar(), UserTypeEnum.MEMBER.getCode(), null);
    }

    /**
     * Member login by username/phone/email + password.
     */
    public LoginVO login(MemberLoginDTO dto) {
        // Find member by username, phone, or email
        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>()
                        .eq(Member::getUsername, dto.getUsername())
                        .or()
                        .eq(Member::getPhone, dto.getUsername())
                        .or()
                        .eq(Member::getEmail, dto.getUsername()));
        if (member == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // Check account status
        if (member.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // Verify password
        if (!PasswordUtil.verify(dto.getPassword(), member.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // Update last login time
        member.setLastLoginTime(LocalDateTime.now());
        memberMapper.updateById(member);

        return buildLoginVO(member.getId(), member.getUsername(), member.getNickname(),
                member.getAvatar(), UserTypeEnum.MEMBER.getCode(), null);
    }

    /**
     * Member login by phone + SMS code.
     */
    public LoginVO phoneLogin(PhoneLoginDTO dto) {
        boolean valid = smsCodeService.verifyCode(dto.getPhone(), dto.getCode(), 1);
        if (!valid) {
            throw new BusinessException("验证码错误或已过期");
        }

        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>().eq(Member::getPhone, dto.getPhone()));
        if (member == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (member.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        member.setLastLoginTime(LocalDateTime.now());
        memberMapper.updateById(member);

        return buildLoginVO(member.getId(), member.getUsername(), member.getNickname(),
                member.getAvatar(), UserTypeEnum.MEMBER.getCode(), null);
    }

    /**
     * WeChat login (MVP: mock implementation).
     * In production, exchange code for openid via WeChat API, then find/create member.
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO wechatLogin(WechatLoginDTO dto) {
        // MVP: mock wechat login, create a test member
        log.info("WeChat login mock, code={}", dto.getCode());

        String mockOpenid = "wx_mock_" + dto.getCode();

        // Check if already bound
        MemberAuth memberAuth = memberAuthMapper.selectOne(
                new LambdaQueryWrapper<MemberAuth>()
                        .eq(MemberAuth::getIdentityType, 1)
                        .eq(MemberAuth::getIdentifier, mockOpenid));

        Member member;
        if (memberAuth != null) {
            member = memberMapper.selectById(memberAuth.getMemberId());
            if (member == null) {
                throw new BusinessException(ResultCode.USER_NOT_FOUND);
            }
        } else {
            // Create new member for wechat user
            member = new Member();
            member.setUsername("wx_" + System.currentTimeMillis());
            member.setNickname("微信用户");
            member.setStatus(1);
            member.setLastLoginTime(LocalDateTime.now());
            memberMapper.insert(member);

            // Create auth binding
            memberAuth = new MemberAuth();
            memberAuth.setMemberId(member.getId());
            memberAuth.setIdentityType(1);
            memberAuth.setIdentifier(mockOpenid);
            memberAuthMapper.insert(memberAuth);
        }

        // Update last login time
        member.setLastLoginTime(LocalDateTime.now());
        memberMapper.updateById(member);

        return buildLoginVO(member.getId(), member.getUsername(), member.getNickname(),
                member.getAvatar(), UserTypeEnum.MEMBER.getCode(), null);
    }

    /**
     * Refresh access token using refresh token.
     * Implements refresh token rotation: the old refresh token is blacklisted
     * after use to prevent replay attacks.
     */
    public LoginVO refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException("非刷新令牌");
        }

        // Check blacklist
        String blackKey = RedisKeyConstant.TOKEN_BLACKLIST + refreshToken;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blackKey))) {
            // Token already blacklisted — potential theft detected
            Long userId = jwtUtil.getUserId(refreshToken);
            log.warn("Potential token theft detected for userId={}", userId);
            throw new BusinessException(ResultCode.TOKEN_EXPIRED);
        }

        // Blacklist the old refresh token (rotation) with its remaining TTL
        Date tokenExpiry = jwtUtil.getClaimsFromToken(refreshToken).getExpiration();
        long remainingMs = tokenExpiry.getTime() - System.currentTimeMillis();
        if (remainingMs > 0) {
            redisTemplate.opsForValue().set(blackKey, "1", remainingMs, TimeUnit.MILLISECONDS);
        } else {
            // Token is already expired at this point (edge case), still blacklist briefly
            redisTemplate.opsForValue().set(blackKey, "1", 1, TimeUnit.MILLISECONDS);
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        Integer userType = jwtUtil.getUserType(refreshToken);
        Long tenantId = jwtUtil.getTenantId(refreshToken);

        String username = null;
        String nickname = null;
        String avatar = null;

        if (userType == UserTypeEnum.MEMBER.getCode()) {
            Member member = memberMapper.selectById(userId);
            if (member == null) {
                throw new BusinessException(ResultCode.USER_NOT_FOUND);
            }
            username = member.getUsername();
            nickname = member.getNickname();
            avatar = member.getAvatar();
        }

        return buildLoginVO(userId, username, nickname, avatar, userType, tenantId);
    }

    /**
     * Logout: add current token to blacklist.
     */
    public void logout(String token) {
        if (token != null && jwtUtil.validateToken(token)) {
            long expiration = jwtUtil.getExpiration();
            String blackKey = RedisKeyConstant.TOKEN_BLACKLIST + token;
            redisTemplate.opsForValue().set(blackKey, "1", expiration, TimeUnit.MILLISECONDS);
        }
    }

    private LoginVO buildLoginVO(Long userId, String username, String nickname,
                                  String avatar, Integer userType, Long tenantId) {
        String accessToken = jwtUtil.generateToken(userId, userType, tenantId);
        String refreshToken = jwtUtil.generateRefreshToken(userId, userType, tenantId);

        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(accessToken);
        loginVO.setRefreshToken(refreshToken);

        LoginVO.UserInfo userInfo = new LoginVO.UserInfo();
        userInfo.setUserId(userId);
        userInfo.setUsername(username);
        userInfo.setNickname(nickname);
        userInfo.setAvatar(avatar);
        userInfo.setUserType(userType);
        userInfo.setTenantId(tenantId);
        loginVO.setUserInfo(userInfo);

        return loginVO;
    }
}
