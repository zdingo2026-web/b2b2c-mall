package com.mall.service.promotion;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.dao.mapper.member.MemberRedPacketMapper;
import com.mall.dao.mapper.points.PointsAccountMapper;
import com.mall.dao.mapper.promotion.CouponTemplateMapper;
import com.mall.dao.mapper.promotion.FirstOrderConfigMapper;
import com.mall.model.entity.MemberCoupon;
import com.mall.model.entity.member.MemberRedPacket;
import com.mall.model.entity.points.PointsAccount;
import com.mall.model.entity.promotion.CouponTemplate;
import com.mall.model.entity.promotion.FirstOrderConfig;
import com.mall.model.vo.OrderCalculateVO;
import com.mall.model.vo.promotion.CouponOptionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionCalcService {

    private final CouponTemplateMapper couponTemplateMapper;
    private final FirstOrderConfigMapper firstOrderConfigMapper;
    private final PointsAccountMapper pointsAccountMapper;
    private final MemberRedPacketMapper memberRedPacketMapper;

    public OrderCalculateVO calculate(Long memberId, List<Long> spuIds, BigDecimal totalAmount,
                                      Long merchantCouponId, Long platformCouponId,
                                      Boolean usePoints, Boolean useRedPacket, Long redPacketId) {
        OrderCalculateVO vo = new OrderCalculateVO();
        vo.setTotalAmount(totalAmount);
        vo.setFreightAmount(BigDecimal.ZERO);
        vo.setMerchantCouponDiscount(BigDecimal.ZERO);
        vo.setPlatformCouponDiscount(BigDecimal.ZERO);
        vo.setFirstOrderDiscount(BigDecimal.ZERO);
        vo.setPointsDeductAmount(BigDecimal.ZERO);
        vo.setPointsDeductValue(0);
        vo.setRedPacketDiscount(BigDecimal.ZERO);

        BigDecimal payAmount = totalAmount;

        if (merchantCouponId != null) {
            BigDecimal discount = calculateCouponDiscount(merchantCouponId, totalAmount);
            vo.setMerchantCouponDiscount(discount);
            payAmount = payAmount.subtract(discount);
        }

        if (platformCouponId != null) {
            BigDecimal discount = calculateCouponDiscount(platformCouponId, totalAmount);
            vo.setPlatformCouponDiscount(discount);
            payAmount = payAmount.subtract(discount);
        }

        FirstOrderConfig firstOrderConfig = getFirstOrderConfig();
        if (firstOrderConfig != null && firstOrderConfig.getEnabled() != null && firstOrderConfig.getEnabled() == 1) {
            BigDecimal firstOrderDiscount = calculateFirstOrderDiscount(firstOrderConfig, totalAmount);
            vo.setFirstOrderDiscount(firstOrderDiscount);
            payAmount = payAmount.subtract(firstOrderDiscount);
        }

        if (usePoints != null && usePoints) {
            PointsAccount pointsAccount = pointsAccountMapper.selectOne(
                    new LambdaQueryWrapper<PointsAccount>().eq(PointsAccount::getMemberId, memberId));
            if (pointsAccount != null && pointsAccount.getBalance() > 0) {
                int pointsToUse = pointsAccount.getBalance();
                BigDecimal deductRate = new BigDecimal("0.01");
                BigDecimal deductAmount = new BigDecimal(pointsToUse).multiply(deductRate);
                BigDecimal maxDeduct = totalAmount.multiply(new BigDecimal("0.3"));
                if (deductAmount.compareTo(maxDeduct) > 0) {
                    deductAmount = maxDeduct;
                    pointsToUse = maxDeduct.divide(deductRate, 0, RoundingMode.DOWN).intValue();
                }
                vo.setPointsDeductAmount(deductAmount);
                vo.setPointsDeductValue(pointsToUse);
                payAmount = payAmount.subtract(deductAmount);
            }
        }

        if (useRedPacket != null && useRedPacket && redPacketId != null) {
            MemberRedPacket redPacket = memberRedPacketMapper.selectById(redPacketId);
            if (redPacket != null && redPacket.getStatus() != null && redPacket.getStatus() == 0
                    && redPacket.getMemberId().equals(memberId)
                    && redPacket.getValidStartTime().isBefore(LocalDateTime.now())
                    && redPacket.getValidEndTime().isAfter(LocalDateTime.now())) {
                BigDecimal redPacketDiscount = redPacket.getFaceValue();
                if (redPacketDiscount.compareTo(payAmount) > 0) {
                    redPacketDiscount = payAmount;
                }
                vo.setRedPacketDiscount(redPacketDiscount);
                payAmount = payAmount.subtract(redPacketDiscount);
            }
        }

        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }
        vo.setPayAmount(payAmount.setScale(2, RoundingMode.HALF_UP));
        return vo;
    }

    public List<CouponOptionVO> availableCoupons(Long memberId, Long spuId, BigDecimal orderAmount) {
        List<CouponOptionVO> result = new ArrayList<>();
        return result;
    }

    private BigDecimal calculateCouponDiscount(Long couponId, BigDecimal orderAmount) {
        return BigDecimal.ZERO;
    }

    private FirstOrderConfig getFirstOrderConfig() {
        List<FirstOrderConfig> configs = firstOrderConfigMapper.selectList(null);
        return configs.isEmpty() ? null : configs.get(0);
    }

    private BigDecimal calculateFirstOrderDiscount(FirstOrderConfig config, BigDecimal orderAmount) {
        if (config.getDiscountType() == 1) {
            BigDecimal discount = config.getDiscountValue();
            if (config.getMaxDiscount() != null && discount.compareTo(config.getMaxDiscount()) > 0) {
                discount = config.getMaxDiscount();
            }
            return discount;
        } else if (config.getDiscountType() == 2) {
            BigDecimal discount = orderAmount.multiply(BigDecimal.ONE.subtract(
                    config.getDiscountValue().divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP)));
            if (config.getMaxDiscount() != null && discount.compareTo(config.getMaxDiscount()) > 0) {
                discount = config.getMaxDiscount();
            }
            return discount;
        }
        return BigDecimal.ZERO;
    }
}
