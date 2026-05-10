package com.mall.service.distribution;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.OrderItemMapper;
import com.mall.dao.mapper.OrderMainMapper;
import com.mall.dao.mapper.distribution.CommissionRecordMapper;
import com.mall.dao.mapper.distribution.DistributionConfigMapper;
import com.mall.dao.mapper.distribution.DistributionProductMapper;
import com.mall.dao.mapper.distribution.DistributionRelationMapper;
import com.mall.dao.mapper.distribution.DistributorMapper;
import com.mall.dao.mapper.distribution.WithdrawRecordMapper;
import com.mall.model.entity.OrderItem;
import com.mall.model.entity.OrderMain;
import com.mall.model.entity.distribution.CommissionRecord;
import com.mall.model.entity.distribution.DistributionConfig;
import com.mall.model.entity.distribution.DistributionProduct;
import com.mall.model.entity.distribution.DistributionRelation;
import com.mall.model.entity.distribution.Distributor;
import com.mall.model.entity.distribution.WithdrawRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributionSettlementService {

    private final CommissionRecordMapper commissionRecordMapper;
    private final WithdrawRecordMapper withdrawRecordMapper;
    private final DistributorMapper distributorMapper;
    private final DistributionConfigMapper distributionConfigMapper;
    private final DistributionRelationMapper distributionRelationMapper;
    private final DistributionProductMapper distributionProductMapper;
    private final OrderMainMapper orderMainMapper;
    private final OrderItemMapper orderItemMapper;

    @Transactional(rollbackFor = Exception.class)
    public void settleCommission(Long orderId) {
        OrderMain order = orderMainMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        DistributionConfig config = distributionConfigMapper.selectOne(
                new LambdaQueryWrapper<DistributionConfig>().last("LIMIT 1"));
        if (config == null || config.getEnabled() != 1) {
            return;
        }

        DistributionRelation relation = distributionRelationMapper.selectOne(
                new LambdaQueryWrapper<DistributionRelation>()
                        .eq(DistributionRelation::getMemberId, order.getMemberId()));
        if (relation == null) {
            return;
        }

        Long existingCount = commissionRecordMapper.selectCount(
                new LambdaQueryWrapper<CommissionRecord>()
                        .eq(CommissionRecord::getOrderId, orderId));
        if (existingCount > 0) {
            return;
        }

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));

        LocalDateTime unfreezeTime = LocalDateTime.now().plusDays(config.getFreezeDays());

        for (OrderItem item : items) {
            DistributionProduct dp = distributionProductMapper.selectOne(
                    new LambdaQueryWrapper<DistributionProduct>()
                            .eq(DistributionProduct::getSpuId, item.getSpuId()));
            if (dp == null || dp.getCanDistribute() == null || dp.getCanDistribute() != 1) {
                continue;
            }

            BigDecimal baseAmount = config.getCommissionBase() == 1
                    ? item.getPayAmount() : item.getSubtotal();

            BigDecimal rate1 = dp.getUseGlobal() != null && dp.getUseGlobal() == 1
                    ? config.getRateLevel1() : dp.getRateLevel1();
            BigDecimal rate2 = dp.getUseGlobal() != null && dp.getUseGlobal() == 1
                    ? config.getRateLevel2() : dp.getRateLevel2();
            BigDecimal rate3 = dp.getUseGlobal() != null && dp.getUseGlobal() == 1
                    ? config.getRateLevel3() : dp.getRateLevel3();

            settleOneLevel(relation.getParentLevel1(), orderId, item, order.getOrderNo(),
                    baseAmount, rate1, 1, unfreezeTime);
            settleOneLevel(relation.getParentLevel2(), orderId, item, order.getOrderNo(),
                    baseAmount, rate2, 2, unfreezeTime);
            settleOneLevel(relation.getParentLevel3(), orderId, item, order.getOrderNo(),
                    baseAmount, rate3, 3, unfreezeTime);
        }
    }

    private void settleOneLevel(Long distributorId, Long orderId, OrderItem item,
                                String orderNo, BigDecimal baseAmount,
                                BigDecimal rate, int level, LocalDateTime unfreezeTime) {
        if (distributorId == null || rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        Distributor distributor = distributorMapper.selectById(distributorId);
        if (distributor == null || distributor.getStatus() != 1) {
            return;
        }

        BigDecimal commissionAmount = baseAmount.multiply(rate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        CommissionRecord record = new CommissionRecord();
        record.setDistributorId(distributorId);
        record.setOrderId(orderId);
        record.setOrderItemId(item.getId());
        record.setOrderNo(orderNo);
        record.setOrderAmount(baseAmount);
        record.setCommissionRate(rate);
        record.setCommissionAmount(commissionAmount);
        record.setCommissionLevel(level);
        record.setStatus(0);
        record.setUnfreezeTime(unfreezeTime);
        commissionRecordMapper.insert(record);

        distributor.setTotalCommission(distributor.getTotalCommission().add(commissionAmount));
        distributor.setFrozenCommission(distributor.getFrozenCommission().add(commissionAmount));
        distributorMapper.updateById(distributor);
    }

    @Transactional(rollbackFor = Exception.class)
    public void processUnfreeze() {
        List<CommissionRecord> records = commissionRecordMapper.selectList(
                new LambdaQueryWrapper<CommissionRecord>()
                        .eq(CommissionRecord::getStatus, 0)
                        .le(CommissionRecord::getUnfreezeTime, LocalDateTime.now()));

        for (CommissionRecord record : records) {
            record.setStatus(1);
            commissionRecordMapper.updateById(record);

            Distributor distributor = distributorMapper.selectById(record.getDistributorId());
            if (distributor != null) {
                distributor.setFrozenCommission(distributor.getFrozenCommission().subtract(record.getCommissionAmount()));
                distributor.setAvailableCommission(distributor.getAvailableCommission().add(record.getCommissionAmount()));
                distributorMapper.updateById(distributor);
            }
        }
        log.info("Processed unfreeze for {} commission records", records.size());
    }
}
