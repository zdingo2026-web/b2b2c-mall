package com.mall.common.util;

import com.mall.common.constant.RedisKeyConstant;
import com.mall.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RateLimitUtil {
    private final StringRedisTemplate redisTemplate;

    public void checkRateLimit(String key, int maxAttempts, long windowSeconds) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String countStr = ops.get(key);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        if (count >= maxAttempts) {
            throw new BusinessException(429, "操作过于频繁，请稍后重试");
        }
        long newCount = ops.increment(key);
        if (newCount == 1) {
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
    }

    public void clearRateLimit(String key) {
        redisTemplate.delete(key);
    }
}
