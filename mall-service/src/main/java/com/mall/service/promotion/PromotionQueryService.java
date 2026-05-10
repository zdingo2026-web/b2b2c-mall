package com.mall.service.promotion;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.dao.mapper.promotion.DiscountActivityMapper;
import com.mall.dao.mapper.promotion.DiscountProductMapper;
import com.mall.dao.mapper.promotion.GroupBuyActivityMapper;
import com.mall.dao.mapper.promotion.GrouponActivityMapper;
import com.mall.dao.mapper.promotion.GrouponProductMapper;
import com.mall.dao.mapper.promotion.SeckillActivityMapper;
import com.mall.dao.mapper.promotion.SeckillSkuMapper;
import com.mall.model.entity.promotion.DiscountActivity;
import com.mall.model.entity.promotion.DiscountProduct;
import com.mall.model.entity.promotion.GroupBuyActivity;
import com.mall.model.entity.promotion.GrouponActivity;
import com.mall.model.entity.promotion.GrouponProduct;
import com.mall.model.entity.promotion.SeckillActivity;
import com.mall.model.entity.promotion.SeckillSku;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionQueryService {

    private final SeckillActivityMapper seckillActivityMapper;
    private final SeckillSkuMapper seckillSkuMapper;
    private final GroupBuyActivityMapper groupBuyActivityMapper;
    private final GrouponActivityMapper grouponActivityMapper;
    private final GrouponProductMapper grouponProductMapper;
    private final DiscountActivityMapper discountActivityMapper;
    private final DiscountProductMapper discountProductMapper;

    public SeckillSku getSeckillSku(Long skuId) {
        List<SeckillSku> list = seckillSkuMapper.selectList(
                new LambdaQueryWrapper<SeckillSku>()
                        .eq(SeckillSku::getSkuId, skuId));
        if (list.isEmpty()) {
            return null;
        }
        for (SeckillSku sku : list) {
            SeckillActivity activity = seckillActivityMapper.selectById(sku.getActivityId());
            if (activity != null && activity.getStatus() == 1
                    && activity.getStartTime().isBefore(LocalDateTime.now())
                    && activity.getEndTime().isAfter(LocalDateTime.now())) {
                return sku;
            }
        }
        return null;
    }

    public GroupBuyActivity getGroupBuyActivity(Long activityId) {
        GroupBuyActivity activity = groupBuyActivityMapper.selectById(activityId);
        if (activity == null || activity.getStatus() != 1) {
            return null;
        }
        if (activity.getEndTime().isBefore(LocalDateTime.now())) {
            return null;
        }
        return activity;
    }

    public GrouponProduct getGrouponProduct(Long spuId) {
        List<GrouponProduct> products = grouponProductMapper.selectList(
                new LambdaQueryWrapper<GrouponProduct>()
                        .eq(GrouponProduct::getSpuId, spuId));
        if (products.isEmpty()) {
            return null;
        }
        for (GrouponProduct product : products) {
            GrouponActivity activity = grouponActivityMapper.selectById(product.getActivityId());
            if (activity != null && activity.getStatus() == 1
                    && activity.getStartTime().isBefore(LocalDateTime.now())
                    && activity.getEndTime().isAfter(LocalDateTime.now())) {
                return product;
            }
        }
        return null;
    }

    public BigDecimal getDiscountPrice(Long spuId, BigDecimal originalPrice) {
        List<DiscountProduct> products = discountProductMapper.selectList(
                new LambdaQueryWrapper<DiscountProduct>()
                        .eq(DiscountProduct::getSpuId, spuId));
        if (products.isEmpty()) {
            return null;
        }
        for (DiscountProduct product : products) {
            DiscountActivity activity = discountActivityMapper.selectById(product.getActivityId());
            if (activity != null && activity.getStatus() == 1
                    && activity.getStartTime().isBefore(LocalDateTime.now())
                    && activity.getEndTime().isAfter(LocalDateTime.now())) {
                return calculateDiscountPrice(activity, originalPrice);
            }
        }
        return null;
    }

    private BigDecimal calculateDiscountPrice(DiscountActivity activity, BigDecimal originalPrice) {
        BigDecimal discountPrice;
        if (activity.getDiscountType() == 1) {
            discountPrice = originalPrice.subtract(activity.getDiscountValue());
        } else if (activity.getDiscountType() == 2) {
            discountPrice = originalPrice.multiply(
                    activity.getDiscountValue().divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP));
        } else {
            discountPrice = originalPrice;
        }
        if (activity.getMaxDiscount() != null) {
            BigDecimal maxDiscounted = originalPrice.subtract(activity.getMaxDiscount());
            if (discountPrice.compareTo(maxDiscounted) < 0) {
                discountPrice = maxDiscounted;
            }
        }
        if (discountPrice.compareTo(BigDecimal.ZERO) < 0) {
            discountPrice = BigDecimal.ZERO;
        }
        return discountPrice.setScale(2, RoundingMode.HALF_UP);
    }
}
