-- KEYS[1] = seckill:stock:{activityId}:{skuId}  库存key
-- KEYS[2] = seckill:purchased:{activityId}:{skuId}  已购集合key
-- ARGV[1] = memberId
-- ARGV[2] = limitPerUser

-- 检查限购
if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
    return -1
end

-- 检查库存
local stock = tonumber(redis.call('GET', KEYS[1]))
if stock == nil or stock <= 0 then
    return 0
end

-- 扣减库存
redis.call('DECR', KEYS[1])
-- 记录已购
redis.call('SADD', KEYS[2], ARGV[1])
return 1
