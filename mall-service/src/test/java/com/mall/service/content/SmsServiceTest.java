package com.mall.service.content;

import com.mall.common.constant.RedisKeyConstant;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private SmsService smsService;

    private static final String PHONE = "13800138000";

    @BeforeEach
    void setUp() {
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("sendCode")
    class SendCode {

        @Test
        @DisplayName("Sends code successfully when no restrictions apply")
        void sendCode_success() {
            when(stringRedisTemplate.hasKey(RedisKeyConstant.SMS_INTERVAL + PHONE)).thenReturn(false);
            when(valueOperations.get(anyString())).thenReturn(null);
            when(stringRedisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

            smsService.sendCode(PHONE);

            verify(valueOperations).set(
                    eq(RedisKeyConstant.SMS_CODE + PHONE),
                    anyString(),
                    eq(5L),
                    eq(TimeUnit.MINUTES));
            verify(valueOperations).set(
                    eq(RedisKeyConstant.SMS_INTERVAL + PHONE),
                    eq("1"),
                    eq(60L),
                    eq(TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("Throws PARAM_ERROR when phone is null")
        void sendCode_nullPhone() {
            assertThatThrownBy(() -> smsService.sendCode(null))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.PARAM_ERROR.getCode());
        }

        @Test
        @DisplayName("Throws SMS_SEND_FAIL when 60s interval not elapsed")
        void sendCode_intervalNotElapsed() {
            when(stringRedisTemplate.hasKey(RedisKeyConstant.SMS_INTERVAL + PHONE)).thenReturn(true);

            assertThatThrownBy(() -> smsService.sendCode(PHONE))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.SMS_SEND_FAIL.getCode());
        }

        @Test
        @DisplayName("Throws SMS_SEND_FAIL when daily limit reached")
        void sendCode_dailyLimitReached() {
            when(stringRedisTemplate.hasKey(RedisKeyConstant.SMS_INTERVAL + PHONE)).thenReturn(false);

            String dailyCountKey = RedisKeyConstant.SMS_DAILY_COUNT + PHONE + ":" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            when(valueOperations.get(dailyCountKey)).thenReturn("5");

            assertThatThrownBy(() -> smsService.sendCode(PHONE))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.SMS_SEND_FAIL.getCode());
        }
    }

    @Nested
    @DisplayName("verifyCode")
    class VerifyCode {

        @Test
        @DisplayName("Returns true and deletes code when code matches")
        void verifyCode_success() {
            when(valueOperations.get(RedisKeyConstant.SMS_VERIFY_ATTEMPTS + PHONE)).thenReturn(null);
            when(valueOperations.get(RedisKeyConstant.SMS_CODE + PHONE)).thenReturn("123456");

            boolean result = smsService.verifyCode(PHONE, "123456");

            assertThat(result).isTrue();
            verify(stringRedisTemplate).delete(RedisKeyConstant.SMS_CODE + PHONE);
            verify(stringRedisTemplate).delete(RedisKeyConstant.SMS_VERIFY_ATTEMPTS + PHONE);
        }

        @Test
        @DisplayName("Returns false when code does not match")
        void verifyCode_wrongCode() {
            when(valueOperations.get(RedisKeyConstant.SMS_VERIFY_ATTEMPTS + PHONE)).thenReturn(null);
            when(valueOperations.get(RedisKeyConstant.SMS_CODE + PHONE)).thenReturn("123456");
            when(valueOperations.increment(RedisKeyConstant.SMS_VERIFY_ATTEMPTS + PHONE)).thenReturn(1L);
            when(stringRedisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

            boolean result = smsService.verifyCode(PHONE, "654321");

            assertThat(result).isFalse();
            verify(stringRedisTemplate, never()).delete(RedisKeyConstant.SMS_CODE + PHONE);
        }

        @Test
        @DisplayName("Returns false when code is expired or not found")
        void verifyCode_codeExpired() {
            when(valueOperations.get(RedisKeyConstant.SMS_VERIFY_ATTEMPTS + PHONE)).thenReturn(null);
            when(valueOperations.get(RedisKeyConstant.SMS_CODE + PHONE)).thenReturn(null);

            boolean result = smsService.verifyCode(PHONE, "123456");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Returns false when phone is null")
        void verifyCode_nullPhone() {
            boolean result = smsService.verifyCode(null, "123456");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Returns false when code is null")
        void verifyCode_nullCode() {
            boolean result = smsService.verifyCode(PHONE, null);

            assertThat(result).isFalse();
        }
    }
}
