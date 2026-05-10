package com.mall.service.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.TenantMapper;
import com.mall.dao.mapper.tenant.TenantLevelMapper;
import com.mall.model.dto.tenant.TenantLevelDTO;
import com.mall.model.entity.Tenant;
import com.mall.model.entity.tenant.TenantLevel;
import com.mall.model.vo.tenant.TenantLevelVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantLevelService {

    private final TenantLevelMapper tenantLevelMapper;
    private final TenantMapper tenantMapper;

    public List<TenantLevelVO> list() {
        List<TenantLevel> levels = tenantLevelMapper.selectList(
                new LambdaQueryWrapper<TenantLevel>()
                        .orderByAsc(TenantLevel::getSortWeight)
                        .orderByAsc(TenantLevel::getMinScore));
        return levels.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(TenantLevelDTO dto) {
        TenantLevel level = new TenantLevel();
        BeanUtils.copyProperties(dto, level);
        tenantLevelMapper.insert(level);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, TenantLevelDTO dto) {
        TenantLevel level = tenantLevelMapper.selectById(id);
        if (level == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (dto.getLevelName() != null) {
            level.setLevelName(dto.getLevelName());
        }
        if (dto.getLevelIcon() != null) {
            level.setLevelIcon(dto.getLevelIcon());
        }
        if (dto.getCommissionDiscount() != null) {
            level.setCommissionDiscount(dto.getCommissionDiscount());
        }
        if (dto.getMinScore() != null) {
            level.setMinScore(dto.getMinScore());
        }
        if (dto.getSortWeight() != null) {
            level.setSortWeight(dto.getSortWeight());
        }
        tenantLevelMapper.updateById(level);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        TenantLevel level = tenantLevelMapper.selectById(id);
        if (level == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        Long count = tenantMapper.selectCount(
                new LambdaQueryWrapper<Tenant>()
                        .eq(Tenant::getLevelId, id));
        if (count > 0) {
            throw new BusinessException("该等级下存在商家，无法删除");
        }
        tenantLevelMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void recalculateLevels() {
        List<TenantLevel> levels = tenantLevelMapper.selectList(
                new LambdaQueryWrapper<TenantLevel>()
                        .orderByAsc(TenantLevel::getMinScore));
        if (levels.isEmpty()) {
            return;
        }

        List<Tenant> tenants = tenantMapper.selectList(
                new LambdaQueryWrapper<Tenant>()
                        .isNotNull(Tenant::getScoreComposite));
        for (Tenant tenant : tenants) {
            int score = tenant.getScoreComposite() != null
                    ? tenant.getScoreComposite().intValue() : 0;
            TenantLevel matched = null;
            for (TenantLevel level : levels) {
                if (score >= level.getMinScore()) {
                    matched = level;
                }
            }
            if (matched != null && !matched.getId().equals(tenant.getLevelId())) {
                tenant.setLevelId(matched.getId());
                tenantMapper.updateById(tenant);
            }
        }
    }

    private TenantLevelVO toVO(TenantLevel level) {
        TenantLevelVO vo = new TenantLevelVO();
        BeanUtils.copyProperties(level, vo);
        return vo;
    }
}
