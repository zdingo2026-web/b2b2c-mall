package com.mall.service.distribution;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.distribution.DistributionConfigMapper;
import com.mall.model.dto.distribution.DistributionConfigDTO;
import com.mall.model.entity.distribution.DistributionConfig;
import com.mall.model.vo.distribution.DistributionConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributionConfigService {

    private final DistributionConfigMapper distributionConfigMapper;

    public DistributionConfigVO getConfig() {
        DistributionConfig config = distributionConfigMapper.selectOne(
                new LambdaQueryWrapper<DistributionConfig>().last("LIMIT 1"));
        if (config == null) {
            return new DistributionConfigVO();
        }
        return toConfigVO(config);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(DistributionConfigDTO dto) {
        DistributionConfig config = distributionConfigMapper.selectOne(
                new LambdaQueryWrapper<DistributionConfig>().last("LIMIT 1"));
        if (config == null) {
            config = new DistributionConfig();
        }
        if (dto.getEnabled() != null) {
            config.setEnabled(dto.getEnabled() ? 1 : 0);
        }
        if (dto.getAutoAudit() != null) {
            config.setAutoAudit(dto.getAutoAudit() ? 1 : 0);
        }
        config.setCommissionBase(dto.getCommissionBase());
        config.setRateLevel1(dto.getRateLevel1());
        config.setRateLevel2(dto.getRateLevel2());
        config.setRateLevel3(dto.getRateLevel3());
        config.setMinWithdraw(dto.getMinWithdraw());
        config.setFreezeDays(dto.getFreezeDays());
        if (dto.getDailyWithdrawLimit() != null) {
            config.setDailyWithdrawLimit(dto.getDailyWithdrawLimit());
        }
        config.setWithdrawMethods(dto.getWithdrawMethods());

        if (config.getId() == null) {
            distributionConfigMapper.insert(config);
        } else {
            distributionConfigMapper.updateById(config);
        }
    }

    private DistributionConfigVO toConfigVO(DistributionConfig config) {
        DistributionConfigVO vo = new DistributionConfigVO();
        vo.setId(config.getId());
        vo.setEnabled(config.getEnabled() != null && config.getEnabled() == 1);
        vo.setAutoAudit(config.getAutoAudit() != null && config.getAutoAudit() == 1);
        vo.setCommissionBase(config.getCommissionBase());
        vo.setRateLevel1(config.getRateLevel1());
        vo.setRateLevel2(config.getRateLevel2());
        vo.setRateLevel3(config.getRateLevel3());
        vo.setMinWithdraw(config.getMinWithdraw());
        vo.setFreezeDays(config.getFreezeDays());
        vo.setDailyWithdrawLimit(config.getDailyWithdrawLimit());
        vo.setWithdrawMethods(config.getWithdrawMethods());
        return vo;
    }
}
