package com.mall.service.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.tenant.TenantSettleConfigMapper;
import com.mall.model.dto.tenant.TenantSettleConfigDTO;
import com.mall.model.entity.tenant.TenantSettleConfig;
import com.mall.model.vo.tenant.TenantSettleConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantSettleConfigService {

    private final TenantSettleConfigMapper settleConfigMapper;

    public TenantSettleConfigVO getConfig() {
        TenantSettleConfig config = settleConfigMapper.selectOne(
                new LambdaQueryWrapper<TenantSettleConfig>().last("LIMIT 1"));
        if (config == null) {
            TenantSettleConfigVO vo = new TenantSettleConfigVO();
            vo.setEnabled(false);
            vo.setAutoAudit(false);
            return vo;
        }
        TenantSettleConfigVO vo = new TenantSettleConfigVO();
        BeanUtils.copyProperties(config, vo);
        vo.setEnabled(config.getEnabled() != null && config.getEnabled() == 1);
        vo.setAutoAudit(config.getAutoAudit() != null && config.getAutoAudit() == 1);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(TenantSettleConfigDTO dto) {
        TenantSettleConfig config = settleConfigMapper.selectOne(
                new LambdaQueryWrapper<TenantSettleConfig>().last("LIMIT 1"));

        if (config == null) {
            config = new TenantSettleConfig();
            config.setEnabled(dto.getEnabled() != null && dto.getEnabled() ? 1 : 0);
            config.setSettleNotice(dto.getSettleNotice());
            config.setSettleAgreement(dto.getSettleAgreement());
            config.setAutoAudit(dto.getAutoAudit() != null && dto.getAutoAudit() ? 1 : 0);
            settleConfigMapper.insert(config);
        } else {
            if (dto.getEnabled() != null) {
                config.setEnabled(dto.getEnabled() ? 1 : 0);
            }
            if (dto.getSettleNotice() != null) {
                config.setSettleNotice(dto.getSettleNotice());
            }
            if (dto.getSettleAgreement() != null) {
                config.setSettleAgreement(dto.getSettleAgreement());
            }
            if (dto.getAutoAudit() != null) {
                config.setAutoAudit(dto.getAutoAudit() ? 1 : 0);
            }
            settleConfigMapper.updateById(config);
        }
    }
}
