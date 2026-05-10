package com.mall.service.promotion;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.enums.OrderStatusEnum;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.common.util.SnowflakeIdUtil;
import com.mall.dao.mapper.OrderItemMapper;
import com.mall.dao.mapper.OrderMainMapper;
import com.mall.dao.mapper.ProductSkuMapper;
import com.mall.dao.mapper.ProductSpuMapper;
import com.mall.dao.mapper.promotion.SeckillActivityMapper;
import com.mall.dao.mapper.promotion.SeckillSkuMapper;
import com.mall.model.dto.promotion.SeckillActivityCreateDTO;
import com.mall.model.dto.promotion.SeckillSkuDTO;
import com.mall.model.entity.OrderItem;
import com.mall.model.entity.OrderMain;
import com.mall.model.entity.ProductSku;
import com.mall.model.entity.ProductSpu;
import com.mall.model.entity.promotion.SeckillActivity;
import com.mall.model.entity.promotion.SeckillSku;
import com.mall.model.vo.promotion.SeckillActivityDetailVO;
import com.mall.model.vo.promotion.SeckillActivityVO;
import com.mall.model.vo.promotion.SeckillProductVO;
import com.mall.model.vo.promotion.SeckillResultVO;
import com.mall.model.vo.promotion.SeckillTimeSlotVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillActivityService {

    private final SeckillActivityMapper seckillActivityMapper;
    private final SeckillSkuMapper seckillSkuMapper;
    private final ProductSpuMapper productSpuMapper;
    private final ProductSkuMapper productSkuMapper;
    private final OrderMainMapper orderMainMapper;
    private final OrderItemMapper orderItemMapper;
    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final SeckillStockService seckillStockService;

    public PageResult<SeckillActivityVO> list(Long tenantId, Integer status, int page, int limit) {
        Page<SeckillActivity> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(SeckillActivity::getTenantId, tenantId);
        }
        if (status != null) {
            wrapper.eq(SeckillActivity::getStatus, status);
        }
        wrapper.orderByDesc(SeckillActivity::getCreateTime);
        Page<SeckillActivity> result = seckillActivityMapper.selectPage(pageParam, wrapper);
        List<SeckillActivityVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    public SeckillActivityDetailVO detail(Long id) {
        SeckillActivity activity = seckillActivityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        SeckillActivityDetailVO vo = new SeckillActivityDetailVO();
        vo.setId(activity.getId());
        vo.setActivityName(activity.getActivityName());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setPaymentTimeout(activity.getPaymentTimeout());
        vo.setStatus(activity.getStatus());

        List<SeckillSku> skuList = seckillSkuMapper.selectList(
                new LambdaQueryWrapper<SeckillSku>().eq(SeckillSku::getActivityId, id));
        List<SeckillActivityDetailVO.SeckillSkuVO> skuVOs = skuList.stream().map(sku -> {
            SeckillActivityDetailVO.SeckillSkuVO skuVO = new SeckillActivityDetailVO.SeckillSkuVO();
            skuVO.setId(sku.getId());
            skuVO.setSpuId(sku.getSpuId());
            skuVO.setSkuId(sku.getSkuId());
            skuVO.setSeckillPrice(sku.getSeckillPrice());
            skuVO.setSeckillStock(sku.getSeckillStock());
            skuVO.setSeckillSales(sku.getSeckillSales());
            skuVO.setLimitPerUser(sku.getLimitPerUser());
            skuVO.setCanUseCoupon(sku.getCanUseCoupon() != null && sku.getCanUseCoupon() == 1);
            return skuVO;
        }).collect(Collectors.toList());
        vo.setSkuList(skuVOs);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(Long tenantId, SeckillActivityCreateDTO dto) {
        SeckillActivity activity = new SeckillActivity();
        activity.setActivityName(dto.getActivityName());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setPaymentTimeout(dto.getPaymentTimeout());
        activity.setStatus(0);
        activity.setTenantId(tenantId);
        seckillActivityMapper.insert(activity);

        for (SeckillSkuDTO skuDTO : dto.getSkuList()) {
            SeckillSku sku = new SeckillSku();
            sku.setActivityId(activity.getId());
            sku.setSpuId(skuDTO.getSpuId());
            sku.setSkuId(skuDTO.getSkuId());
            sku.setSeckillPrice(skuDTO.getSeckillPrice());
            sku.setSeckillStock(skuDTO.getSeckillStock());
            sku.setSeckillSales(0);
            sku.setLimitPerUser(skuDTO.getLimitPerUser());
            sku.setCanUseCoupon(skuDTO.getCanUseCoupon() != null && skuDTO.getCanUseCoupon() ? 1 : 0);
            sku.setSortOrder(0);
            sku.setTenantId(tenantId);
            seckillSkuMapper.insert(sku);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, SeckillActivityCreateDTO dto) {
        SeckillActivity activity = seckillActivityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        activity.setActivityName(dto.getActivityName());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setPaymentTimeout(dto.getPaymentTimeout());
        seckillActivityMapper.updateById(activity);

        seckillSkuMapper.delete(
                new LambdaQueryWrapper<SeckillSku>().eq(SeckillSku::getActivityId, id));
        for (SeckillSkuDTO skuDTO : dto.getSkuList()) {
            SeckillSku sku = new SeckillSku();
            sku.setActivityId(id);
            sku.setSpuId(skuDTO.getSpuId());
            sku.setSkuId(skuDTO.getSkuId());
            sku.setSeckillPrice(skuDTO.getSeckillPrice());
            sku.setSeckillStock(skuDTO.getSeckillStock());
            sku.setSeckillSales(0);
            sku.setLimitPerUser(skuDTO.getLimitPerUser());
            sku.setCanUseCoupon(skuDTO.getCanUseCoupon() != null && skuDTO.getCanUseCoupon() ? 1 : 0);
            sku.setSortOrder(0);
            sku.setTenantId(activity.getTenantId());
            seckillSkuMapper.insert(sku);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SeckillActivity activity = seckillActivityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        seckillSkuMapper.delete(
                new LambdaQueryWrapper<SeckillSku>().eq(SeckillSku::getActivityId, id));
        seckillActivityMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void startActivity(Long id) {
        SeckillActivity activity = seckillActivityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        activity.setStatus(1);
        seckillActivityMapper.updateById(activity);

        List<SeckillSku> skuList = seckillSkuMapper.selectList(
                new LambdaQueryWrapper<SeckillSku>().eq(SeckillSku::getActivityId, id));
        seckillStockService.preloadStock(id, skuList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void endActivity(Long id) {
        SeckillActivity activity = seckillActivityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        activity.setStatus(2);
        seckillActivityMapper.updateById(activity);
        seckillStockService.cleanUpStock(id);
    }

    public SeckillResultVO getSeckillResult(Long memberId, Long activityId, Long skuId) {
        SeckillResultVO result = new SeckillResultVO();
        OrderMain order = orderMainMapper.selectOne(
                new LambdaQueryWrapper<OrderMain>()
                        .eq(OrderMain::getMemberId, memberId)
                        .eq(OrderMain::getOrderType, 2)
                        .eq(OrderMain::getActivityId, activityId)
                        .orderByDesc(OrderMain::getCreateTime)
                        .last("LIMIT 1"));
        if (order != null) {
            result.setStatus("success");
            result.setOrderNo(order.getOrderNo());
        } else {
            Boolean memberSeckilled = redisTemplate.hasKey("seckill:stock:" + activityId + ":" + skuId + ":member:" + memberId);
            if (memberSeckilled != null && memberSeckilled) {
                result.setStatus("waiting");
            } else {
                result.setStatus("fail");
            }
        }
        return result;
    }

    public List<SeckillTimeSlotVO> timeSlots() {
        List<SeckillTimeSlotVO> slots = new ArrayList<>();
        int[] hours = {8, 10, 12, 14, 16, 18, 20, 22};
        LocalDateTime now = LocalDateTime.now();
        for (int hour : hours) {
            SeckillTimeSlotVO slot = new SeckillTimeSlotVO();
            slot.setTimeSlot(String.format("%02d:00", hour));
            slot.setStartTime(now.with(LocalTime.of(hour, 0)));
            slot.setEndTime(now.with(LocalTime.of(hour + 2, 0)));
            slot.setStatus(now.getHour() >= hour && now.getHour() < hour + 2 ? 1 : (now.getHour() < hour ? 0 : 2));
            slots.add(slot);
        }
        return slots;
    }

    public PageResult<SeckillProductVO> products(Long activityId, int page, int limit) {
        Page<SeckillSku> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<SeckillSku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillSku::getActivityId, activityId);
        wrapper.orderByAsc(SeckillSku::getSortOrder);
        Page<SeckillSku> result = seckillSkuMapper.selectPage(pageParam, wrapper);

        List<SeckillProductVO> voList = result.getRecords().stream().map(sku -> {
            SeckillProductVO vo = new SeckillProductVO();
            vo.setId(sku.getId());
            vo.setActivityId(sku.getActivityId());
            vo.setSpuId(sku.getSpuId());
            vo.setSkuId(sku.getSkuId());
            vo.setSeckillPrice(sku.getSeckillPrice());
            vo.setSeckillStock(sku.getSeckillStock());
            vo.setSeckillSales(sku.getSeckillSales());
            vo.setLimitPerUser(sku.getLimitPerUser());
            vo.setCanUseCoupon(sku.getCanUseCoupon() != null && sku.getCanUseCoupon() == 1);

            ProductSpu spu = productSpuMapper.selectById(sku.getSpuId());
            if (spu != null) {
                vo.setSpuName(spu.getProductName());
                vo.setMainImage(spu.getMainImage());
            }
            ProductSku productSku = productSkuMapper.selectById(sku.getSkuId());
            if (productSku != null) {
                vo.setOriginalPrice(productSku.getPrice());
            }
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    public SeckillResultVO seckill(Long memberId, Long activityId, Long skuId) {
        SeckillActivity activity = seckillActivityMapper.selectById(activityId);
        if (activity == null || activity.getStatus() != 1) {
            throw new BusinessException("活动未开始或已结束");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            throw new BusinessException("不在活动时间内");
        }

        SeckillSku seckillSku = seckillSkuMapper.selectOne(
                new LambdaQueryWrapper<SeckillSku>()
                        .eq(SeckillSku::getActivityId, activityId)
                        .eq(SeckillSku::getSkuId, skuId));
        if (seckillSku == null) {
            throw new BusinessException("秒杀商品不存在");
        }

        boolean success = seckillStockService.deductStock(activityId, skuId, memberId, seckillSku.getLimitPerUser());
        if (!success) {
            SeckillResultVO failResult = new SeckillResultVO();
            failResult.setStatus("fail");
            return failResult;
        }

        Map<String, Object> orderMsg = new HashMap<>();
        orderMsg.put("memberId", memberId);
        orderMsg.put("activityId", activityId);
        orderMsg.put("skuId", skuId);
        orderMsg.put("seckillPrice", seckillSku.getSeckillPrice());
        orderMsg.put("paymentTimeout", activity.getPaymentTimeout());
        rabbitTemplate.convertAndSend("mall.seckill.order.exchange", "mall.seckill.order", orderMsg);

        SeckillResultVO result = new SeckillResultVO();
        result.setStatus("success");
        return result;
    }

    /**
     * Create seckill order (called by MQ consumer).
     * Creates an order with orderType=2 (seckill order).
     */
    @Transactional(rollbackFor = Exception.class)
    public void createSeckillOrder(Long memberId, Long activityId, Long skuId) {
        SeckillActivity activity = seckillActivityMapper.selectById(activityId);
        if (activity == null) {
            log.warn("Seckill activity not found: activityId={}", activityId);
            return;
        }

        SeckillSku seckillSku = seckillSkuMapper.selectOne(
                new LambdaQueryWrapper<SeckillSku>()
                        .eq(SeckillSku::getActivityId, activityId)
                        .eq(SeckillSku::getSkuId, skuId));
        if (seckillSku == null) {
            log.warn("Seckill sku not found: activityId={}, skuId={}", activityId, skuId);
            return;
        }

        ProductSku productSku = productSkuMapper.selectById(skuId);
        if (productSku == null) {
            log.warn("Product SKU not found: skuId={}", skuId);
            return;
        }

        ProductSpu productSpu = productSpuMapper.selectById(seckillSku.getSpuId());

        // Create order
        String orderNo = String.valueOf(SnowflakeIdUtil.getInstance().nextId());
        int paymentTimeoutMinutes = activity.getPaymentTimeout() != null ? activity.getPaymentTimeout() : 30;

        OrderMain order = new OrderMain();
        order.setOrderNo(orderNo);
        order.setMemberId(memberId);
        order.setParentId(0L);
        order.setOrderType(2); // 秒杀订单
        order.setActivityId(activityId);
        order.setOrderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());
        order.setPayStatus(0);
        order.setTotalAmount(seckillSku.getSeckillPrice());
        order.setFreightAmount(java.math.BigDecimal.ZERO);
        order.setDiscountAmount(java.math.BigDecimal.ZERO);
        order.setPayAmount(seckillSku.getSeckillPrice());
        order.setDeliveryType(1);
        order.setExpireTime(LocalDateTime.now().plusMinutes(paymentTimeoutMinutes));
        order.setVersion(1);
        order.setTenantId(activity.getTenantId());
        orderMainMapper.insert(order);

        // Create order item
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setSpuId(seckillSku.getSpuId());
        orderItem.setSkuId(skuId);
        orderItem.setProductName(productSpu != null ? productSpu.getProductName() : "");
        orderItem.setSpecValues(productSku.getSpecValues());
        orderItem.setProductImage(productSku.getImage() != null ? productSku.getImage()
                : (productSpu != null ? productSpu.getMainImage() : ""));
        orderItem.setPrice(seckillSku.getSeckillPrice());
        orderItem.setQuantity(1);
        orderItem.setSubtotal(seckillSku.getSeckillPrice());
        orderItem.setPayAmount(seckillSku.getSeckillPrice());
        orderItem.setTenantId(activity.getTenantId());
        orderItemMapper.insert(orderItem);

        // Update seckill sales count
        seckillSku.setSeckillSales(seckillSku.getSeckillSales() + 1);
        seckillSkuMapper.updateById(seckillSku);

        log.info("Seckill order created: orderNo={}, memberId={}, activityId={}, skuId={}",
                orderNo, memberId, activityId, skuId);
    }

    /**
     * Sync seckill activity status.
     * Automatically ends activities whose end time has passed.
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncActivityStatus() {
        LocalDateTime now = LocalDateTime.now();
        List<SeckillActivity> expiredActivities = seckillActivityMapper.selectList(
                new LambdaQueryWrapper<SeckillActivity>()
                        .eq(SeckillActivity::getStatus, 1)
                        .le(SeckillActivity::getEndTime, now));

        for (SeckillActivity activity : expiredActivities) {
            activity.setStatus(2);
            seckillActivityMapper.updateById(activity);
            seckillStockService.cleanUpStock(activity.getId());
            log.info("Seckill activity auto-ended: id={}, name={}", activity.getId(), activity.getActivityName());
        }

        if (!expiredActivities.isEmpty()) {
            log.info("Synced {} seckill activities to ended status", expiredActivities.size());
        }
    }

    private SeckillActivityVO toVO(SeckillActivity activity) {
        SeckillActivityVO vo = new SeckillActivityVO();
        vo.setId(activity.getId());
        vo.setActivityName(activity.getActivityName());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setPaymentTimeout(activity.getPaymentTimeout());
        vo.setStatus(activity.getStatus());
        vo.setCreateTime(activity.getCreateTime());
        return vo;
    }
}
