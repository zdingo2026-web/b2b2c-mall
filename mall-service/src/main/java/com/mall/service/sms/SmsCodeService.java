package com.mall.service.sms;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.constant.RedisKeyConstant;
import com.mall.common.exception.BusinessException;
import com.mall.dao.mapper.SmsCodeMapper;
import com.mall.model.entity.SmsCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * SMS verification code service.
 * Security mechanisms:
 * 1. 60-second interval between sends (Redis throttle)
 * 2. Daily send limit per phone (max 10)
 * 3. Verification code expires in 5 minutes
 * 4. Max 5 verification attempts per code
 * 5. Code invalidated after successful verification (one-time use)
 * 6. All codes persisted to database for audit trail
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsCodeService {

    private final SmsCodeMapper smsCodeMapper;
    private final StringRedisTemplate redisTemplate;

    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int SEND_INTERVAL_SECONDS = 60;
    private static final int DAILY_LIMIT = 10;
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    /**
     * Send SMS verification code.
     *
     * @param phone   target phone number
     * @param bizType business type (1-login, 2-register, 3-change password, 4-bind phone)
     * @param ip      requester IP address
     */
    public void sendCode(String phone, int bizType, String ip) {
        // 1. Check 60-second interval (Redis)
        String intervalKey = RedisKeyConstant.SMS_INTERVAL + phone;
        String existing = redisTemplate.opsForValue().get(intervalKey);
        if (existing != null) {
            long remaining = redisTemplate.getExpire(intervalKey, TimeUnit.SECONDS);
            throw new BusinessException("操作过于频繁，请" + (remaining > 0 ? remaining : 1) + "秒后重试");
        }

        // 2. Check daily limit (Redis counter)
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String countKey = RedisKeyConstant.SMS_DAILY_COUNT + phone + ":" + today;
        String countStr = redisTemplate.opsForValue().get(countKey);
        int dailyCount = countStr != null ? Integer.parseInt(countStr) : 0;
        if (dailyCount >= DAILY_LIMIT) {
            throw new BusinessException("今日发送次数已达上限");
        }

        // 3. Generate 6-digit code
        String code = generateCode();

        // 4. Invalidate previous unused codes for this phone+bizType
        smsCodeMapper.update(null, new LambdaUpdateWrapper<SmsCode>()
                .eq(SmsCode::getPhone, phone)
                .eq(SmsCode::getBizType, bizType)
                .eq(SmsCode::getVerified, 0)
                .lt(SmsCode::getExpireAt, LocalDateTime.now())
                .set(SmsCode::getVerified, 2)); // 2 = expired/invalidated

        // 5. Persist to database
        SmsCode smsCode = new SmsCode();
        smsCode.setPhone(phone);
        smsCode.setCode(code);
        smsCode.setBizType(bizType);
        smsCode.setIp(ip);
        smsCode.setExpireAt(LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES));
        smsCode.setVerified(0);
        smsCode.setVerifyAttempts(0);
        smsCode.setCreateTime(LocalDateTime.now());
        smsCode.setUpdateTime(LocalDateTime.now());
        smsCodeMapper.insert(smsCode);

        // 6. Store in Redis for fast verification
        String redisKey = RedisKeyConstant.SMS_CODE + phone;
        redisTemplate.opsForValue().set(redisKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 7. Set interval throttle
        redisTemplate.opsForValue().set(intervalKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // 8. Increment daily counter
        long ttl = redisTemplate.getExpire(countKey, TimeUnit.SECONDS);
        if (ttl > 0) {
            redisTemplate.opsForValue().increment(countKey);
        } else {
            redisTemplate.opsForValue().set(countKey, String.valueOf(dailyCount + 1),
                    86400, TimeUnit.SECONDS); // 24h TTL
        }

        // 9. Send SMS (MVP: log instead of real SMS gateway)
        log.info("SMS code sent: phone={}, bizType={}, code={}", phone, bizType, code);

        // TODO: Integrate real SMS gateway (Aliyun SMS, Tencent SMS, etc.)
        // smsGateway.send(phone, code, bizType);
    }

    /**
     * Verify SMS code.
     *
     * @param phone   phone number
     * @param code    verification code
     * @param bizType business type
     * @return true if code is valid
     */
    public boolean verifyCode(String phone, String code, int bizType) {
        // 1. Fast check from Redis
        String redisKey = RedisKeyConstant.SMS_CODE + phone;
        String cachedCode = redisTemplate.opsForValue().get(redisKey);

        if (cachedCode != null && cachedCode.equals(code)) {
            // Mark as verified in database
            markVerified(phone, code, bizType);
            // Delete from Redis (one-time use)
            redisTemplate.delete(redisKey);
            return true;
        }

        // 2. Fallback: check database (in case Redis expired but code is still valid)
        SmsCode smsCode = smsCodeMapper.selectOne(new LambdaQueryWrapper<SmsCode>()
                .eq(SmsCode::getPhone, phone)
                .eq(SmsCode::getBizType, bizType)
                .eq(SmsCode::getCode, code)
                .eq(SmsCode::getVerified, 0)
                .gt(SmsCode::getExpireAt, LocalDateTime.now())
                .orderByDesc(SmsCode::getCreateTime)
                .last("LIMIT 1"));

        if (smsCode == null) {
            // Increment verify attempts on the latest code for this phone
            incrementAttempts(phone, bizType);
            return false;
        }

        // Check max attempts
        if (smsCode.getVerifyAttempts() >= MAX_VERIFY_ATTEMPTS) {
            throw new BusinessException("验证尝试次数过多，请重新获取验证码");
        }

        // Mark as verified
        markVerified(phone, code, bizType);
        // Delete from Redis
        redisTemplate.delete(redisKey);
        return true;
    }

    private void markVerified(String phone, String code, int bizType) {
        smsCodeMapper.update(null, new LambdaUpdateWrapper<SmsCode>()
                .eq(SmsCode::getPhone, phone)
                .eq(SmsCode::getCode, code)
                .eq(SmsCode::getBizType, bizType)
                .eq(SmsCode::getVerified, 0)
                .set(SmsCode::getVerified, 1)
                .set(SmsCode::getUpdateTime, LocalDateTime.now()));
    }

    private void incrementAttempts(String phone, int bizType) {
        SmsCode latest = smsCodeMapper.selectOne(new LambdaQueryWrapper<SmsCode>()
                .eq(SmsCode::getPhone, phone)
                .eq(SmsCode::getBizType, bizType)
                .eq(SmsCode::getVerified, 0)
                .gt(SmsCode::getExpireAt, LocalDateTime.now())
                .orderByDesc(SmsCode::getCreateTime)
                .last("LIMIT 1"));

        if (latest != null) {
            smsCodeMapper.update(null, new LambdaUpdateWrapper<SmsCode>()
                    .eq(SmsCode::getId, latest.getId())
                    .set(SmsCode::getVerifyAttempts, latest.getVerifyAttempts() + 1)
                    .set(SmsCode::getUpdateTime, LocalDateTime.now()));

            if (latest.getVerifyAttempts() + 1 >= MAX_VERIFY_ATTEMPTS) {
                // Invalidate this code
                smsCodeMapper.update(null, new LambdaUpdateWrapper<SmsCode>()
                        .eq(SmsCode::getId, latest.getId())
                        .set(SmsCode::getVerified, 2));
                // Also delete from Redis
                redisTemplate.delete(RedisKeyConstant.SMS_CODE + phone);
                throw new BusinessException("验证尝试次数过多，请重新获取验证码");
            }
        }
    }

    private String generateCode() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
