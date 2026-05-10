package com.mall.service.promotion;

import com.mall.dao.mapper.promotion.CouponTemplateMapper;
import com.mall.model.entity.promotion.CouponTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponStockService {

    private final StringRedisTemplate redisTemplate;
    private final CouponTemplateMapper couponTemplateMapper;

    private static final String STOCK_KEY_PREFIX = "coupon:stock:";
    private static final String CLAIMED_KEY_PREFIX = "coupon:claimed:";

    private static final String DEDUCT_LUA_SCRIPT =
            "local stockKey = KEYS[1]\n" +
            "local claimedKey = KEYS[2]\n" +
            "local memberId = ARGV[1]\n" +
            "local limitPerUser = tonumber(ARGV[2])\n" +
            "local claimed = tonumber(redis.call('sismember', claimedKey, memberId) or '0')\n" +
            "if claimed >= limitPerUser then\n" +
            "    return -1\n" +
            "end\n" +
            "local stock = tonumber(redis.call('get', stockKey) or '0')\n" +
            "if stock <= 0 then\n" +
            "    return 0\n" +
            "end\n" +
            "redis.call('decr', stockKey)\n" +
            "redis.call('sadd', claimedKey, memberId)\n" +
            "return 1";

    public void preloadStock(Long templateId, int totalCount) {
        String stockKey = STOCK_KEY_PREFIX + templateId;
        String claimedKey = CLAIMED_KEY_PREFIX + templateId;
        redisTemplate.opsForValue().set(stockKey, String.valueOf(totalCount));
        redisTemplate.delete(claimedKey);
        log.info("Preloaded coupon stock: templateId={}, totalCount={}", templateId, totalCount);
    }

    public boolean deductStock(Long templateId, Long memberId, int limitPerUser) {
        String stockKey = STOCK_KEY_PREFIX + templateId;
        String claimedKey = CLAIMED_KEY_PREFIX + templateId;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(DEDUCT_LUA_SCRIPT, Long.class);
        List<String> keys = new ArrayList<>();
        keys.add(stockKey);
        keys.add(claimedKey);

        Long result = redisTemplate.execute(script, keys, String.valueOf(memberId), String.valueOf(limitPerUser));
        if (result == null) {
            return false;
        }
        if (result == -1L) {
            log.info("Coupon claim limit reached: memberId={}, templateId={}", memberId, templateId);
            return false;
        }
        if (result == 0L) {
            log.info("Coupon stock exhausted: templateId={}", templateId);
            return false;
        }
        return true;
    }

    public void rollbackStock(Long templateId) {
        String stockKey = STOCK_KEY_PREFIX + templateId;
        redisTemplate.opsForValue().increment(stockKey);
        log.info("Rolled back coupon stock: templateId={}", templateId);
    }
}
