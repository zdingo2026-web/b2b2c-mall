-- KEYS[1] = coupon:stock:{templateId}  库存key
-- KEYS[2] = coupon:claimed:{templateId}  已领集合key
-- ARGV[1] = memberId
-- ARGV[2] = limitPerUser

-- 检查限领
local claimed = tonumber(redis.call('SCARD', KEYS[2])) or 0
-- 注意：这里用集合大小不够精确，改用SISMEMBER
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
-- 记录已领
redis.call('SADD', KEYS[2], ARGV[1])
return 1
