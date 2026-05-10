package com.mall.service.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.OrderItemMapper;
import com.mall.dao.mapper.OrderMainMapper;
import com.mall.dao.mapper.TenantMapper;
import com.mall.dao.mapper.tenant.TenantCommissionSettlementMapper;
import com.mall.model.entity.OrderItem;
import com.mall.model.entity.OrderMain;
import com.mall.model.entity.Tenant;
import com.mall.model.entity.tenant.TenantCommissionSettlement;
import com.mall.model.vo.tenant.TenantSettlementDetailVO;
import com.mall.model.vo.tenant.TenantSettlementVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantCommissionSettlementService {

    private final TenantCommissionSettlementMapper settlementMapper;
    private final OrderMainMapper orderMainMapper;
    private final OrderItemMapper orderItemMapper;
    private final TenantMapper tenantMapper;

    public PageResult<TenantSettlementVO> list(Long tenantId, Integer status, int page, int limit) {
        Page<TenantCommissionSettlement> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<TenantCommissionSettlement> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(TenantCommissionSettlement::getTenantId, tenantId);
        }
        if (status != null) {
            wrapper.eq(TenantCommissionSettlement::getStatus, status);
        }
        wrapper.orderByDesc(TenantCommissionSettlement::getCreateTime);

        Page<TenantCommissionSettlement> result = settlementMapper.selectPage(pageParam, wrapper);

        List<Long> tenantIds = result.getRecords().stream()
                .map(TenantCommissionSettlement::getTenantId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> tenantNameMap = tenantIds.isEmpty() ? Collections.emptyMap() :
                tenantMapper.selectBatchIds(tenantIds).stream()
                        .collect(Collectors.toMap(Tenant::getId, Tenant::getTenantName, (a, b) -> a));

        List<TenantSettlementVO> voList = result.getRecords().stream().map(s -> {
            TenantSettlementVO vo = new TenantSettlementVO();
            BeanUtils.copyProperties(s, vo);
            vo.setTenantName(tenantNameMap.get(s.getTenantId()));
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    public TenantSettlementDetailVO detail(Long id) {
        TenantCommissionSettlement settlement = settlementMapper.selectById(id);
        if (settlement == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        TenantSettlementDetailVO vo = new TenantSettlementDetailVO();
        BeanUtils.copyProperties(settlement, vo);

        Tenant tenant = tenantMapper.selectById(settlement.getTenantId());
        if (tenant != null) {
            vo.setTenantName(tenant.getTenantName());
        }
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void generateSettlement(Long tenantId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BusinessException(ResultCode.TENANT_NOT_FOUND);
        }

        LambdaQueryWrapper<OrderMain> orderWrapper = new LambdaQueryWrapper<OrderMain>()
                .eq(OrderMain::getTenantId, tenantId)
                .eq(OrderMain::getOrderStatus, 3)
                .ge(OrderMain::getReceiveTime, periodStart)
                .lt(OrderMain::getReceiveTime, periodEnd);
        List<OrderMain> orders = orderMainMapper.selectList(orderWrapper);

        BigDecimal orderTotalAmount = BigDecimal.ZERO;
        for (OrderMain order : orders) {
            orderTotalAmount = orderTotalAmount.add(order.getPayAmount() != null ? order.getPayAmount() : BigDecimal.ZERO);
        }

        BigDecimal commissionRate = tenant.getCommissionRate() != null
                ? tenant.getCommissionRate().divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal platformCommission = orderTotalAmount.multiply(commissionRate)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal merchantAmount = orderTotalAmount.subtract(platformCommission)
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        TenantCommissionSettlement settlement = new TenantCommissionSettlement();
        settlement.setSettlementNo(generateSettlementNo(tenantId));
        settlement.setTenantId(tenantId);
        settlement.setPeriodStart(periodStart);
        settlement.setPeriodEnd(periodEnd);
        settlement.setOrderCount(orders.size());
        settlement.setOrderTotalAmount(orderTotalAmount.setScale(2, BigDecimal.ROUND_HALF_UP));
        settlement.setPlatformCommission(platformCommission);
        settlement.setMerchantAmount(merchantAmount);
        settlement.setStatus(0);
        settlementMapper.insert(settlement);
    }

    @Transactional(rollbackFor = Exception.class)
    public void settle(Long id) {
        TenantCommissionSettlement settlement = settlementMapper.selectById(id);
        if (settlement == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (settlement.getStatus() != 0) {
            throw new BusinessException("结算单状态异常");
        }
        settlement.setStatus(1);
        settlement.setSettleTime(LocalDateTime.now());
        settlementMapper.updateById(settlement);
    }

    private String generateSettlementNo(Long tenantId) {
        return "ST" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", tenantId % 10000)
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
