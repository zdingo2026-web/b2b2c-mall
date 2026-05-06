package com.mall.service.content;

import com.mall.common.constant.RedisKeyConstant;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * SMS service.
 * Generates and verifies SMS verification codes stored in Redis.
 * MVP: Mock sending, real logic uses Aliyun SMS.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final int CODE_LENGTH = 6;
    private static final long CODE_TTL_MINUTES = 5;
    private static final long INTERVAL_SECONDS = 60;
    private static final int DAILY_LIMIT = 5;
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    /**
     * Send a verification code to the specified phone number.
     * - Validates 60-second interval
     * - Validates daily limit (5 per phone per day)
     * - Generates 6-digit code
     * - Stores in Redis with 5-minute TTL
     *
     * @param phone phone number
     */
    public void sendCode(String phone) {
        if (phone == null || phone.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "手机号不能为空");
        }

        // Check 60-second interval
        String intervalKey = RedisKeyConstant.SMS_INTERVAL + phone;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(intervalKey))) {
            throw new BusinessException(ResultCode.SMS_SEND_FAIL.getCode(), "发送过于频繁，请60秒后重试");
        }

        // Check daily limit
        String dailyCountKey = RedisKeyConstant.SMS_DAILY_COUNT + phone + ":" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String countStr = stringRedisTemplate.opsForValue().get(dailyCountKey);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        if (count >= DAILY_LIMIT) {
            throw new BusinessException(ResultCode.SMS_SEND_FAIL.getCode(), "今日发送次数已达上限");
        }

        // Generate 6-digit code
        String code = generateCode();

        // Store code in Redis with 5-minute TTL
        String codeKey = RedisKeyConstant.SMS_CODE + phone;
        stringRedisTemplate.opsForValue().set(codeKey, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);

        // Set interval key
        stringRedisTemplate.opsForValue().set(intervalKey, "1", INTERVAL_SECONDS, TimeUnit.SECONDS);

        // Increment daily count
        stringRedisTemplate.opsForValue().increment(dailyCountKey);
        stringRedisTemplate.expire(dailyCountKey, 1, TimeUnit.DAYS);

        // Mock send
        log.info("[SmsService] Mock send code: phone={}, code={}", phone, code);
    }

    /**
     * Verify the SMS code for the given phone number.
     * Includes brute-force protection: max 5 verify attempts per code.
     *
     * @param phone phone number
     * @param code  verification code
     * @return true if the code is valid
     */
    public boolean verifyCode(String phone, String code) {
        if (phone == null || code == null) {
            return false;
        }

        // Check verify attempts (brute-force protection, S-08)
        String attemptsKey = RedisKeyConstant.SMS_VERIFY_ATTEMPTS + phone;
        String attemptsStr = stringRedisTemplate.opsForValue().get(attemptsKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;
        if (attempts >= MAX_VERIFY_ATTEMPTS) {
            // Invalidate the code since max attempts reached
            String codeKey = RedisKeyConstant.SMS_CODE + phone;
            stringRedisTemplate.delete(codeKey);
            stringRedisTemplate.delete(attemptsKey);
            throw new BusinessException(ResultCode.SMS_SEND_FAIL.getCode(), "验证尝试次数过多，请重新获取验证码");
        }

        String codeKey = RedisKeyConstant.SMS_CODE + phone;
        String storedCode = stringRedisTemplate.opsForValue().get(codeKey);

        if (storedCode == null) {
            log.warn("[SmsService] Code not found or expired: phone={}", phone);
            return false;
        }

        if (storedCode.equals(code)) {
            // Delete code after successful verification (one-time use)
            stringRedisTemplate.delete(codeKey);
            // Clear verify attempts on success
            stringRedisTemplate.delete(attemptsKey);
            return true;
        }

        // Increment verify attempts on failure
        long newAttempts = stringRedisTemplate.opsForValue().increment(attemptsKey);
        if (newAttempts == 1) {
            // Set TTL to match code TTL so attempts expire with the code
            stringRedisTemplate.expire(attemptsKey, CODE_TTL_MINUTES, TimeUnit.MINUTES);
        }
        if (newAttempts >= MAX_VERIFY_ATTEMPTS) {
            // Max attempts reached - invalidate code
            stringRedisTemplate.delete(codeKey);
            stringRedisTemplate.delete(attemptsKey);
            throw new BusinessException(ResultCode.SMS_SEND_FAIL.getCode(), "验证尝试次数过多，请重新获取验证码");
        }

        log.warn("[SmsService] Invalid code: phone={}, attempts={}/{}", phone, newAttempts, MAX_VERIFY_ATTEMPTS);
        return false;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
