package com.mall.service.distribution;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.OrderItemMapper;
import com.mall.dao.mapper.OrderMainMapper;
import com.mall.dao.mapper.distribution.CommissionRecordMapper;
import com.mall.dao.mapper.distribution.DistributionConfigMapper;
import com.mall.dao.mapper.distribution.DistributorMapper;
import com.mall.model.entity.OrderItem;
import com.mall.model.entity.OrderMain;
import com.mall.model.entity.distribution.CommissionRecord;
import com.mall.model.entity.distribution.DistributionConfig;
import com.mall.model.entity.distribution.DistributionProduct;
import com.mall.model.entity.distribution.DistributionRelation;
import com.mall.model.entity.distribution.Distributor;
import com.mall.model.vo.distribution.CommissionRecordVO;
import com.mall.model.vo.distribution.CommissionTrendVO;
import com.mall.model.vo.distribution.DistributionOrderVO;
import com.mall.model.vo.distribution.DistributionStatVO;
import com.mall.model.vo.distribution.DistributorRankVO;
import com.mall.dao.mapper.distribution.DistributionProductMapper;
import com.mall.dao.mapper.distribution.DistributionRelationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommissionService {

    private final CommissionRecordMapper commissionRecordMapper;
    private final DistributorMapper distributorMapper;
    private final DistributionConfigMapper distributionConfigMapper;
    private final OrderMainMapper orderMainMapper;
    private final OrderItemMapper orderItemMapper;
    private final DistributionRelationMapper distributionRelationMapper;
    private final DistributionProductMapper distributionProductMapper;

    @Transactional(rollbackFor = Exception.class)
    public void calculateCommission(Long orderId) {
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

            createCommissionRecord(relation.getParentLevel1(), orderId, item, order.getOrderNo(),
                    baseAmount, rate1, 1, unfreezeTime);
            createCommissionRecord(relation.getParentLevel2(), orderId, item, order.getOrderNo(),
                    baseAmount, rate2, 2, unfreezeTime);
            createCommissionRecord(relation.getParentLevel3(), orderId, item, order.getOrderNo(),
                    baseAmount, rate3, 3, unfreezeTime);
        }
    }

    private void createCommissionRecord(Long distributorId, Long orderId, OrderItem item,
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
    public void unfreezeCommission(Long distributorId, Long commissionId) {
        CommissionRecord record = commissionRecordMapper.selectById(commissionId);
        if (record == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (distributorId != null && !record.getDistributorId().equals(distributorId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (record.getStatus() != 0) {
            throw new BusinessException("佣金状态异常");
        }

        record.setStatus(1);
        commissionRecordMapper.updateById(record);

        Distributor distributor = distributorMapper.selectById(record.getDistributorId());
        distributor.setFrozenCommission(distributor.getFrozenCommission().subtract(record.getCommissionAmount()));
        distributor.setAvailableCommission(distributor.getAvailableCommission().add(record.getCommissionAmount()));
        distributorMapper.updateById(distributor);
    }

    public PageResult<CommissionRecordVO> commissionList(Long distributorId, Integer status, int page, int limit) {
        Page<CommissionRecord> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<CommissionRecord> wrapper = new LambdaQueryWrapper<CommissionRecord>()
                .eq(CommissionRecord::getDistributorId, distributorId);
        if (status != null) {
            wrapper.eq(CommissionRecord::getStatus, status);
        }
        wrapper.orderByDesc(CommissionRecord::getCreateTime);

        Page<CommissionRecord> result = commissionRecordMapper.selectPage(pageParam, wrapper);
        List<CommissionRecordVO> voList = result.getRecords().stream()
                .map(this::toCommissionRecordVO)
                .collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    public PageResult<DistributionOrderVO> orderList(Long distributorId, int page, int limit) {
        Page<CommissionRecord> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<CommissionRecord> wrapper = new LambdaQueryWrapper<CommissionRecord>()
                .eq(CommissionRecord::getDistributorId, distributorId)
                .orderByDesc(CommissionRecord::getCreateTime);

        Page<CommissionRecord> result = commissionRecordMapper.selectPage(pageParam, wrapper);
        List<DistributionOrderVO> voList = result.getRecords().stream()
                .map(this::toDistributionOrderVO)
                .collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    public DistributionStatVO stat(Long distributorId) {
        Distributor distributor = distributorMapper.selectById(distributorId);
        if (distributor == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        DistributionStatVO vo = new DistributionStatVO();
        vo.setTotalCommission(distributor.getTotalCommission());
        vo.setAvailableCommission(distributor.getAvailableCommission());
        vo.setFrozenCommission(distributor.getFrozenCommission());

        Long totalOrders = commissionRecordMapper.selectCount(
                new LambdaQueryWrapper<CommissionRecord>()
                        .eq(CommissionRecord::getDistributorId, distributorId));
        vo.setTotalOrders(totalOrders.intValue());

        Long totalDistributors = distributorMapper.selectCount(
                new LambdaQueryWrapper<Distributor>()
                        .eq(Distributor::getParentId, distributorId));
        vo.setTotalDistributors(totalDistributors.intValue());
        return vo;
    }

    public List<CommissionTrendVO> commissionTrend(Long distributorId, Integer days) {
        if (days == null || days <= 0) {
            days = 7;
        }
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        List<CommissionTrendVO> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            CommissionTrendVO vo = new CommissionTrendVO();
            vo.setDate(date.format(formatter));

            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

            List<CommissionRecord> records = commissionRecordMapper.selectList(
                    new LambdaQueryWrapper<CommissionRecord>()
                            .eq(CommissionRecord::getDistributorId, distributorId)
                            .ge(CommissionRecord::getCreateTime, dayStart)
                            .le(CommissionRecord::getCreateTime, dayEnd));

            BigDecimal totalAmount = records.stream()
                    .map(CommissionRecord::getCommissionAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setCommissionAmount(totalAmount);
            vo.setOrderCount(records.size());
            result.add(vo);
        }
        return result;
    }

    public List<DistributorRankVO> rankList(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        List<Distributor> distributors = distributorMapper.selectList(
                new LambdaQueryWrapper<Distributor>()
                        .eq(Distributor::getStatus, 1)
                        .orderByDesc(Distributor::getTotalCommission)
                        .last("LIMIT " + limit));

        return distributors.stream().map(d -> {
            DistributorRankVO vo = new DistributorRankVO();
            vo.setDistributorId(d.getId());
            vo.setRealName(d.getRealName());
            vo.setPhone(d.getPhone());
            vo.setTotalCommission(d.getTotalCommission());
            Long orderCount = commissionRecordMapper.selectCount(
                    new LambdaQueryWrapper<CommissionRecord>()
                            .eq(CommissionRecord::getDistributorId, d.getId()));
            vo.setOrderCount(orderCount.intValue());
            return vo;
        }).collect(Collectors.toList());
    }

    private CommissionRecordVO toCommissionRecordVO(CommissionRecord r) {
        CommissionRecordVO vo = new CommissionRecordVO();
        vo.setId(r.getId());
        vo.setDistributorId(r.getDistributorId());
        vo.setOrderNo(r.getOrderNo());
        vo.setOrderAmount(r.getOrderAmount());
        vo.setCommissionRate(r.getCommissionRate());
        vo.setCommissionAmount(r.getCommissionAmount());
        vo.setCommissionLevel(r.getCommissionLevel());
        vo.setStatus(r.getStatus());
        vo.setUnfreezeTime(r.getUnfreezeTime());
        vo.setCreateTime(r.getCreateTime());
        return vo;
    }

    private DistributionOrderVO toDistributionOrderVO(CommissionRecord r) {
        DistributionOrderVO vo = new DistributionOrderVO();
        vo.setOrderItemId(r.getOrderItemId());
        vo.setOrderNo(r.getOrderNo());
        vo.setOrderAmount(r.getOrderAmount());
        vo.setCommissionRate(r.getCommissionRate());
        vo.setCommissionAmount(r.getCommissionAmount());
        vo.setCommissionLevel(r.getCommissionLevel());
        vo.setCommissionStatus(r.getStatus());
        vo.setCreateTime(r.getCreateTime());

        OrderMain order = orderMainMapper.selectById(r.getOrderId());
        if (order != null) {
            vo.setMemberId(order.getMemberId());
        }
        return vo;
    }
}
