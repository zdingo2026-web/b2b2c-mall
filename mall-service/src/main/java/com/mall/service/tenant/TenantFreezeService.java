package com.mall.service.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.TenantMapper;
import com.mall.dao.mapper.tenant.TenantFreezeRecordMapper;
import com.mall.model.dto.tenant.TenantFreezeDTO;
import com.mall.model.entity.Tenant;
import com.mall.model.entity.tenant.TenantFreezeRecord;
import com.mall.model.vo.tenant.TenantFreezeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantFreezeService {

    private final TenantFreezeRecordMapper freezeRecordMapper;
    private final TenantMapper tenantMapper;

    public PageResult<TenantFreezeVO> list(Long tenantId, int page, int limit) {
        Page<TenantFreezeRecord> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<TenantFreezeRecord> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(TenantFreezeRecord::getTenantId, tenantId);
        }
        wrapper.orderByDesc(TenantFreezeRecord::getCreateTime);

        Page<TenantFreezeRecord> result = freezeRecordMapper.selectPage(pageParam, wrapper);

        List<Long> tenantIds = result.getRecords().stream()
                .map(TenantFreezeRecord::getTenantId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> tenantNameMap = tenantIds.isEmpty() ? Collections.emptyMap() :
                tenantMapper.selectBatchIds(tenantIds).stream()
                        .collect(Collectors.toMap(Tenant::getId, Tenant::getTenantName, (a, b) -> a));

        List<TenantFreezeVO> voList = result.getRecords().stream().map(r -> {
            TenantFreezeVO vo = new TenantFreezeVO();
            BeanUtils.copyProperties(r, vo);
            vo.setTenantName(tenantNameMap.get(r.getTenantId()));
            vo.setNotifyMerchant(r.getNotifyMerchant() != null && r.getNotifyMerchant() == 1);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    @Transactional(rollbackFor = Exception.class)
    public void freeze(Long tenantId, TenantFreezeDTO dto, Long operatorId) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BusinessException(ResultCode.TENANT_NOT_FOUND);
        }

        TenantFreezeRecord record = new TenantFreezeRecord();
        record.setTenantId(tenantId);
        record.setActionType(dto.getActionType());
        record.setReason(dto.getReason());
        record.setNotifyMerchant(dto.getNotifyMerchant() != null && dto.getNotifyMerchant() ? 1 : 0);
        record.setOperatorId(operatorId);
        if (dto.getUnfreezeTime() != null && !dto.getUnfreezeTime().isEmpty()) {
            record.setUnfreezeTime(LocalDateTime.parse(dto.getUnfreezeTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        freezeRecordMapper.insert(record);

        tenant.setFreezeStatus(dto.getActionType());
        tenantMapper.updateById(tenant);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unfreeze(Long tenantId, Long operatorId) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BusinessException(ResultCode.TENANT_NOT_FOUND);
        }
        if (tenant.getFreezeStatus() == null || tenant.getFreezeStatus() == 0) {
            throw new BusinessException("商家未处于冻结状态");
        }

        TenantFreezeRecord record = new TenantFreezeRecord();
        record.setTenantId(tenantId);
        record.setActionType(0);
        record.setReason("解冻商家");
        record.setOperatorId(operatorId);
        record.setNotifyMerchant(1);
        freezeRecordMapper.insert(record);

        tenant.setFreezeStatus(0);
        tenantMapper.updateById(tenant);
    }
}
