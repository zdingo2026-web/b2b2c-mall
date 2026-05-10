package com.mall.service.promotion;

import com.mall.model.entity.promotion.SeckillSku;
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
public class SeckillStockService {

    private final StringRedisTemplate redisTemplate;

    private static final String STOCK_KEY_PREFIX = "seckill:stock:";
    private static final String PURCHASED_KEY_PREFIX = "seckill:purchased:";

    private static final String DEDUCT_LUA_SCRIPT =
            "local stockKey = KEYS[1]\n" +
            "local purchasedKey = KEYS[2]\n" +
            "local memberId = ARGV[1]\n" +
            "local limitPerUser = tonumber(ARGV[2])\n" +
            "local purchased = tonumber(redis.call('sismember', purchasedKey, memberId) or '0')\n" +
            "if purchased >= limitPerUser then\n" +
            "    return -1\n" +
            "end\n" +
            "local stock = tonumber(redis.call('get', stockKey) or '0')\n" +
            "if stock <= 0 then\n" +
            "    return 0\n" +
            "end\n" +
            "redis.call('decr', stockKey)\n" +
            "redis.call('sadd', purchasedKey, memberId)\n" +
            "return 1";

    public void preloadStock(Long activityId, List<SeckillSku> skuList) {
        for (SeckillSku sku : skuList) {
            String stockKey = STOCK_KEY_PREFIX + activityId + ":" + sku.getSkuId();
            redisTemplate.opsForValue().set(stockKey, String.valueOf(sku.getSeckillStock()));
            String purchasedKey = PURCHASED_KEY_PREFIX + activityId + ":" + sku.getSkuId();
            redisTemplate.delete(purchasedKey);
            log.info("Preloaded seckill stock: activityId={}, skuId={}, stock={}", activityId, sku.getSkuId(), sku.getSeckillStock());
        }
    }

    public boolean deductStock(Long activityId, Long skuId, Long memberId, int limitPerUser) {
        String stockKey = STOCK_KEY_PREFIX + activityId + ":" + skuId;
        String purchasedKey = PURCHASED_KEY_PREFIX + activityId + ":" + skuId;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(DEDUCT_LUA_SCRIPT, Long.class);
        List<String> keys = new ArrayList<>();
        keys.add(stockKey);
        keys.add(purchasedKey);

        Long result = redisTemplate.execute(script, keys, String.valueOf(memberId), String.valueOf(limitPerUser));
        if (result == null) {
            return false;
        }
        if (result == -1L) {
            log.info("Seckill limit reached: memberId={}, activityId={}, skuId={}", memberId, activityId, skuId);
            return false;
        }
        if (result == 0L) {
            log.info("Seckill stock exhausted: activityId={}, skuId={}", activityId, skuId);
            return false;
        }
        return true;
    }

    public void rollbackStock(Long activityId, Long skuId) {
        String stockKey = STOCK_KEY_PREFIX + activityId + ":" + skuId;
        redisTemplate.opsForValue().increment(stockKey);
        log.info("Rolled back seckill stock: activityId={}, skuId={}", activityId, skuId);
    }

    public void cleanUpStock(Long activityId) {
        String stockPattern = STOCK_KEY_PREFIX + activityId + ":*";
        String purchasedPattern = PURCHASED_KEY_PREFIX + activityId + ":*";
        redisTemplate.delete(redisTemplate.keys(stockPattern));
        redisTemplate.delete(redisTemplate.keys(purchasedPattern));
        log.info("Cleaned up seckill stock: activityId={}", activityId);
    }
}
