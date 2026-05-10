package com.mall.service.promotion;

import com.mall.dao.mapper.promotion.FirstOrderConfigMapper;
import com.mall.model.dto.promotion.FirstOrderConfigDTO;
import com.mall.model.entity.promotion.FirstOrderConfig;
import com.mall.model.vo.promotion.FirstOrderConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirstOrderConfigService {

    private final FirstOrderConfigMapper firstOrderConfigMapper;

    public FirstOrderConfigVO getConfig() {
        List<FirstOrderConfig> configs = firstOrderConfigMapper.selectList(null);
        if (configs.isEmpty()) {
            FirstOrderConfigVO vo = new FirstOrderConfigVO();
            vo.setEnabled(false);
            return vo;
        }
        FirstOrderConfig config = configs.get(0);
        return toVO(config);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(FirstOrderConfigDTO dto) {
        List<FirstOrderConfig> configs = firstOrderConfigMapper.selectList(null);
        FirstOrderConfig config;
        if (configs.isEmpty()) {
            config = new FirstOrderConfig();
            config.setEnabled(dto.getEnabled() != null && dto.getEnabled() ? 1 : 0);
            config.setDiscountType(dto.getDiscountType());
            config.setDiscountValue(dto.getDiscountValue());
            config.setMaxDiscount(dto.getMaxDiscount());
            config.setApplyScope(dto.getApplyScope());
            config.setApplyCategoryIds(dto.getApplyCategoryIds());
            firstOrderConfigMapper.insert(config);
        } else {
            config = configs.get(0);
            if (dto.getEnabled() != null) {
                config.setEnabled(dto.getEnabled() ? 1 : 0);
            }
            if (dto.getDiscountType() != null) {
                config.setDiscountType(dto.getDiscountType());
            }
            if (dto.getDiscountValue() != null) {
                config.setDiscountValue(dto.getDiscountValue());
            }
            if (dto.getMaxDiscount() != null) {
                config.setMaxDiscount(dto.getMaxDiscount());
            }
            if (dto.getApplyScope() != null) {
                config.setApplyScope(dto.getApplyScope());
            }
            if (dto.getApplyCategoryIds() != null) {
                config.setApplyCategoryIds(dto.getApplyCategoryIds());
            }
            firstOrderConfigMapper.updateById(config);
        }
    }

    private FirstOrderConfigVO toVO(FirstOrderConfig config) {
        FirstOrderConfigVO vo = new FirstOrderConfigVO();
        vo.setId(config.getId());
        vo.setEnabled(config.getEnabled() != null && config.getEnabled() == 1);
        vo.setDiscountType(config.getDiscountType());
        vo.setDiscountValue(config.getDiscountValue());
        vo.setMaxDiscount(config.getMaxDiscount());
        vo.setApplyScope(config.getApplyScope());
        vo.setApplyCategoryIds(config.getApplyCategoryIds());
        return vo;
    }
}
