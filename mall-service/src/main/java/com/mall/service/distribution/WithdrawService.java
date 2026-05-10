package com.mall.service.distribution;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.distribution.DistributionConfigMapper;
import com.mall.dao.mapper.distribution.DistributorMapper;
import com.mall.dao.mapper.distribution.WithdrawRecordMapper;
import com.mall.model.dto.distribution.WithdrawApplyDTO;
import com.mall.model.entity.distribution.DistributionConfig;
import com.mall.model.entity.distribution.Distributor;
import com.mall.model.entity.distribution.WithdrawRecord;
import com.mall.model.vo.distribution.WithdrawRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawService {

    private final WithdrawRecordMapper withdrawRecordMapper;
    private final DistributorMapper distributorMapper;
    private final DistributionConfigMapper distributionConfigMapper;

    @Transactional(rollbackFor = Exception.class)
    public void apply(Long distributorId, WithdrawApplyDTO dto) {
        Distributor distributor = distributorMapper.selectById(distributorId);
        if (distributor == null || distributor.getStatus() != 1) {
            throw new BusinessException("非有效分销员");
        }

        DistributionConfig config = distributionConfigMapper.selectOne(
                new LambdaQueryWrapper<DistributionConfig>().last("LIMIT 1"));
        if (config == null) {
            throw new BusinessException("分销配置不存在");
        }

        if (dto.getAmount().compareTo(config.getMinWithdraw()) < 0) {
            throw new BusinessException("提现金额低于最低限额");
        }
        if (distributor.getAvailableCommission().compareTo(dto.getAmount()) < 0) {
            throw new BusinessException("可提现佣金不足");
        }

        if (config.getDailyWithdrawLimit() != null) {
            LocalDateTime todayStart = LocalDateTime.now().with(java.time.LocalTime.MIN);
            List<WithdrawRecord> todayRecords = withdrawRecordMapper.selectList(
                    new LambdaQueryWrapper<WithdrawRecord>()
                            .eq(WithdrawRecord::getDistributorId, distributorId)
                            .ge(WithdrawRecord::getCreateTime, todayStart)
                            .ne(WithdrawRecord::getStatus, 3));
            BigDecimal todayTotal = todayRecords.stream()
                    .map(WithdrawRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (todayTotal.add(dto.getAmount()).compareTo(config.getDailyWithdrawLimit()) > 0) {
                throw new BusinessException("超出每日提现限额");
            }
        }

        WithdrawRecord record = new WithdrawRecord();
        record.setDistributorId(distributorId);
        record.setAmount(dto.getAmount());
        record.setWithdrawMethod(dto.getWithdrawMethod());
        record.setAccountInfo(dto.getAccountInfo());
        record.setStatus(0);
        withdrawRecordMapper.insert(record);

        distributor.setAvailableCommission(distributor.getAvailableCommission().subtract(dto.getAmount()));
        distributorMapper.updateById(distributor);
    }

    public PageResult<WithdrawRecordVO> list(Long distributorId, Integer status, int page, int limit) {
        Page<WithdrawRecord> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<WithdrawRecord> wrapper = new LambdaQueryWrapper<WithdrawRecord>()
                .eq(WithdrawRecord::getDistributorId, distributorId);
        if (status != null) {
            wrapper.eq(WithdrawRecord::getStatus, status);
        }
        wrapper.orderByDesc(WithdrawRecord::getCreateTime);

        Page<WithdrawRecord> result = withdrawRecordMapper.selectPage(pageParam, wrapper);
        List<WithdrawRecordVO> voList = result.getRecords().stream()
                .map(this::toWithdrawRecordVO)
                .collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    public PageResult<WithdrawRecordVO> adminList(Integer status, int page, int limit) {
        Page<WithdrawRecord> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<WithdrawRecord> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(WithdrawRecord::getStatus, status);
        }
        wrapper.orderByDesc(WithdrawRecord::getCreateTime);

        Page<WithdrawRecord> result = withdrawRecordMapper.selectPage(pageParam, wrapper);
        List<WithdrawRecordVO> voList = result.getRecords().stream()
                .map(this::toWithdrawRecordVO)
                .collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    @Transactional(rollbackFor = Exception.class)
    public void audit(Long withdrawId, Integer status, String rejectReason, Long auditorId) {
        WithdrawRecord record = withdrawRecordMapper.selectById(withdrawId);
        if (record == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (record.getStatus() != 0) {
            throw new BusinessException("该提现申请不在待审核状态");
        }

        record.setStatus(status);
        record.setAuditTime(LocalDateTime.now());
        record.setAuditorId(auditorId);

        if (status == 2) {
            record.setRejectReason(rejectReason);
            Distributor distributor = distributorMapper.selectById(record.getDistributorId());
            distributor.setAvailableCommission(distributor.getAvailableCommission().add(record.getAmount()));
            distributorMapper.updateById(distributor);
        }

        withdrawRecordMapper.updateById(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public void markPaid(Long withdrawId, String paymentRemark) {
        WithdrawRecord record = withdrawRecordMapper.selectById(withdrawId);
        if (record == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (record.getStatus() != 1) {
            throw new BusinessException("该提现申请不在审核通过状态");
        }

        record.setStatus(3);
        record.setPaymentRemark(paymentRemark);
        record.setPaymentTime(LocalDateTime.now());
        withdrawRecordMapper.updateById(record);
    }

    private WithdrawRecordVO toWithdrawRecordVO(WithdrawRecord r) {
        WithdrawRecordVO vo = new WithdrawRecordVO();
        vo.setId(r.getId());
        vo.setAmount(r.getAmount());
        vo.setWithdrawMethod(r.getWithdrawMethod());
        vo.setStatus(r.getStatus());
        vo.setRejectReason(r.getRejectReason());
        vo.setAuditTime(r.getAuditTime());
        vo.setCreateTime(r.getCreateTime());
        return vo;
    }
}
