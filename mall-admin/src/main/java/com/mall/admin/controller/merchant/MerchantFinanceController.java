package com.mall.admin.controller.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.constant.RedisKeyConstant;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.response.ResultCode;
import com.mall.common.util.SnowflakeIdUtil;
import com.mall.common.util.UserContext;
import com.mall.dao.mapper.TenantMapper;
import com.mall.dao.mapper.TenantSettleMapper;
import com.mall.model.dto.WithdrawDTO;
import com.mall.model.entity.Tenant;
import com.mall.model.entity.TenantSettle;
import com.mall.model.vo.BalanceVO;
import com.mall.service.system.SysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Merchant finance controller.
 * Provides balance, settlement records, and withdrawal.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/merchant/finance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantFinanceController {

    private final TenantSettleMapper tenantSettleMapper;
    private final TenantMapper tenantMapper;
    private final SysConfigService sysConfigService;
    private final StringRedisTemplate stringRedisTemplate;

    @GetMapping("/balance")
    public R<BalanceVO> getBalance() {
        Long tenantId = UserContext.getTenantId();
        BalanceVO vo = new BalanceVO();

        BigDecimal totalSettled = sumSettleAmount(tenantId, 1);
        BigDecimal pendingWithdrawal = sumSettleAmount(tenantId, 0);

        vo.setTotalSettled(totalSettled);
        vo.setPendingWithdrawal(pendingWithdrawal);
        vo.setAvailableBalance(totalSettled.subtract(pendingWithdrawal));
        return R.ok(vo);
    }

    @GetMapping("/settle/list")
    public R<PageResult<TenantSettle>> settleList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long tenantId = UserContext.getTenantId();
        Page<TenantSettle> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<TenantSettle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSettle::getTenantId, tenantId)
                .orderByDesc(TenantSettle::getCreateTime);
        Page<TenantSettle> result = tenantSettleMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, limit));
    }

    @PostMapping("/withdraw")
    public R<Void> withdraw(@Validated @RequestBody WithdrawDTO dto) {
        Long tenantId = UserContext.getTenantId();
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        BigDecimal minWithdraw = sysConfigService.getConfigDecimal("min_withdraw_amount");
        if (minWithdraw != null && dto.getAmount().compareTo(minWithdraw) < 0) {
            throw new BusinessException("提现金额不能低于" + minWithdraw + "元");
        }

        // Validate withdrawal amount does not exceed available balance
        BigDecimal totalSettled = sumSettleAmount(tenantId, 1);
        BigDecimal pendingWithdrawal = sumSettleAmount(tenantId, 0);
        BigDecimal availableBalance = totalSettled.subtract(pendingWithdrawal);
        if (dto.getAmount().compareTo(availableBalance) > 0) {
            throw new BusinessException("提现金额超出可用余额");
        }

        // Distributed lock per tenantId to prevent concurrent withdrawals
        String lockKey = RedisKeyConstant.WITHDRAW_LOCK + tenantId;
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            throw new BusinessException("提现处理中，请勿重复操作");
        }

        try {
            // Re-check balance after acquiring lock (balance may have changed)
            BigDecimal totalSettledLocked = sumSettleAmount(tenantId, 1);
            BigDecimal pendingWithdrawalLocked = sumSettleAmount(tenantId, 0);
            BigDecimal availableBalanceLocked = totalSettledLocked.subtract(pendingWithdrawalLocked);
            if (dto.getAmount().compareTo(availableBalanceLocked) > 0) {
                throw new BusinessException("提现金额超出可用余额");
            }

            TenantSettle settle = new TenantSettle();
            settle.setTenantId(tenantId);
            settle.setSettleNo("WD" + SnowflakeIdUtil.getInstance().nextIdStr());
            settle.setSettleAmount(dto.getAmount());
            settle.setOrderAmount(BigDecimal.ZERO);
            settle.setCommissionAmount(BigDecimal.ZERO);
            settle.setStatus(0);
            settle.setPeriodStart(LocalDateTime.now());
            settle.setPeriodEnd(LocalDateTime.now());
            tenantSettleMapper.insert(settle);
        } finally {
            stringRedisTemplate.delete(lockKey);
        }

        return R.ok();
    }

    private BigDecimal sumSettleAmount(Long tenantId, int status) {
        List<TenantSettle> records = tenantSettleMapper.selectList(
                new LambdaQueryWrapper<TenantSettle>()
                        .eq(TenantSettle::getTenantId, tenantId)
                        .eq(TenantSettle::getStatus, status));
        return records.stream()
                .map(TenantSettle::getSettleAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
