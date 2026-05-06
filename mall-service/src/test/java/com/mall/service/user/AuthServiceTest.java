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
import com.mall.model.entity.Member;
import com.mall.model.entity.MemberAuth;
import com.mall.model.vo.LoginVO;
import com.mall.service.sms.SmsCodeService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private MemberAuthMapper memberAuthMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SmsCodeService smsCodeService;

    @InjectMocks
    private AuthService authService;

    private static final String PHONE = "13800138000";
    private static final String PASSWORD = "password123";
    private static final String CODE = "123456";

    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setId(1L);
        member.setPhone(PHONE);
        member.setUsername(PHONE);
        member.setNickname("用户38000");
        member.setPassword(PasswordUtil.encode(PASSWORD));
        member.setStatus(1);
        member.setAvatar("avatar.jpg");

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("register")
    class Register {

        private MemberRegisterDTO dto;

        @BeforeEach
        void setUp() {
            dto = new MemberRegisterDTO();
            dto.setPhone(PHONE);
            dto.setCode(CODE);
            dto.setPassword(PASSWORD);
        }

        @Test
        @DisplayName("Registers new member successfully")
        void register_success() {
            when(memberMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(smsCodeService.verifyCode(PHONE, CODE, 2)).thenReturn(true);
            // Set member ID after insert via Answer
            when(memberMapper.insert(any(Member.class))).thenAnswer(invocation -> {
                Member m = invocation.getArgument(0);
                m.setId(1L);
                return 1;
            });
            when(jwtUtil.generateToken(any(), anyInt(), any())).thenReturn("accessToken");
            when(jwtUtil.generateRefreshToken(any(), anyInt(), any())).thenReturn("refreshToken");

            LoginVO result = authService.register(dto);

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("accessToken");
            assertThat(result.getRefreshToken()).isEqualTo("refreshToken");
            assertThat(result.getUserInfo().getUserType()).isEqualTo(UserTypeEnum.MEMBER.getCode());
        }

        @Test
        @DisplayName("Throws USER_PHONE_EXISTS when phone is already registered")
        void register_phoneExists() {
            when(memberMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            assertThatThrownBy(() -> authService.register(dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.USER_PHONE_EXISTS.getCode());
        }

        @Test
        @DisplayName("Throws error when SMS code is incorrect")
        void register_wrongCode() {
            when(memberMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(smsCodeService.verifyCode(PHONE, CODE, 2)).thenReturn(false);

            assertThatThrownBy(() -> authService.register(dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("验证码错误");
        }

        @Test
        @DisplayName("Throws error when SMS code is expired")
        void register_expiredCode() {
            when(memberMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(smsCodeService.verifyCode(PHONE, CODE, 2)).thenReturn(false);

            assertThatThrownBy(() -> authService.register(dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("验证码错误");
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        private MemberLoginDTO dto;

        @BeforeEach
        void setUp() {
            dto = new MemberLoginDTO();
            dto.setUsername(PHONE);
            dto.setPassword(PASSWORD);
        }

        @Test
        @DisplayName("Logs in successfully with correct credentials")
        void login_success() {
            when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);
            when(memberMapper.updateById(any(Member.class))).thenReturn(1);
            when(jwtUtil.generateToken(anyLong(), anyInt(), any())).thenReturn("accessToken");
            when(jwtUtil.generateRefreshToken(anyLong(), anyInt(), any())).thenReturn("refreshToken");

            LoginVO result = authService.login(dto);

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("accessToken");
            verify(memberMapper).updateById(any(Member.class));
        }

        @Test
        @DisplayName("Throws USER_NOT_FOUND when user does not exist")
        void login_userNotFound() {
            when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            assertThatThrownBy(() -> authService.login(dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.USER_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("Throws ACCOUNT_DISABLED when account is disabled")
        void login_accountDisabled() {
            member.setStatus(0);
            when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);

            assertThatThrownBy(() -> authService.login(dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.ACCOUNT_DISABLED.getCode());
        }

        @Test
        @DisplayName("Throws USER_PASSWORD_ERROR when password is wrong")
        void login_wrongPassword() {
            dto.setPassword("wrongpassword");
            when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);

            assertThatThrownBy(() -> authService.login(dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.USER_PASSWORD_ERROR.getCode());
        }
    }

    @Nested
    @DisplayName("refreshToken")
    class RefreshToken {

        @Test
        @DisplayName("Refreshes token successfully")
        void refreshToken_success() {
            String refreshToken = "validRefreshToken";
            when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
            when(jwtUtil.isRefreshToken(refreshToken)).thenReturn(true);
            when(redisTemplate.hasKey(anyString())).thenReturn(false);
            when(jwtUtil.getUserId(refreshToken)).thenReturn(1L);
            when(jwtUtil.getUserType(refreshToken)).thenReturn(UserTypeEnum.MEMBER.getCode());
            when(jwtUtil.getTenantId(refreshToken)).thenReturn(null);
            when(memberMapper.selectById(1L)).thenReturn(member);
            when(jwtUtil.generateToken(anyLong(), anyInt(), any())).thenReturn("newAccessToken");
            when(jwtUtil.generateRefreshToken(anyLong(), anyInt(), any())).thenReturn("newRefreshToken");

            // Mock Claims for refresh token rotation (blacklisting old token with remaining TTL)
            Claims claims = mock(Claims.class);
            when(claims.getExpiration()).thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
            when(jwtUtil.getClaimsFromToken(refreshToken)).thenReturn(claims);

            LoginVO result = authService.refreshToken(refreshToken);

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("newAccessToken");
        }

        @Test
        @DisplayName("Throws TOKEN_INVALID when token is invalid")
        void refreshToken_invalidToken() {
            when(jwtUtil.validateToken("invalid")).thenReturn(false);

            assertThatThrownBy(() -> authService.refreshToken("invalid"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.TOKEN_INVALID.getCode());
        }

        @Test
        @DisplayName("Throws error when using access token instead of refresh token")
        void refreshToken_notRefreshToken() {
            when(jwtUtil.validateToken("accessToken")).thenReturn(true);
            when(jwtUtil.isRefreshToken("accessToken")).thenReturn(false);

            assertThatThrownBy(() -> authService.refreshToken("accessToken"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("非刷新令牌");
        }

        @Test
        @DisplayName("Throws TOKEN_EXPIRED when token is blacklisted")
        void refreshToken_blacklisted() {
            when(jwtUtil.validateToken("token")).thenReturn(true);
            when(jwtUtil.isRefreshToken("token")).thenReturn(true);
            when(redisTemplate.hasKey(anyString())).thenReturn(true);

            assertThatThrownBy(() -> authService.refreshToken("token"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.TOKEN_EXPIRED.getCode());
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("Adds token to blacklist on logout")
        void logout_success() {
            when(jwtUtil.validateToken("validToken")).thenReturn(true);
            when(jwtUtil.getExpiration()).thenReturn(86400000L);

            authService.logout("validToken");

            verify(valueOperations).set(
                    eq(RedisKeyConstant.TOKEN_BLACKLIST + "validToken"),
                    eq("1"),
                    eq(86400000L),
                    eq(TimeUnit.MILLISECONDS));
        }

        @Test
        @DisplayName("Does nothing when token is null")
        void logout_nullToken() {
            authService.logout(null);

            verify(redisTemplate, never()).opsForValue();
        }

        @Test
        @DisplayName("Does nothing when token is invalid")
        void logout_invalidToken() {
            when(jwtUtil.validateToken("invalid")).thenReturn(false);

            authService.logout("invalid");

            verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        }
    }
}
