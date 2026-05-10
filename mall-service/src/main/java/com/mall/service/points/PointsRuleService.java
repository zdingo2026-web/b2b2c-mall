package com.mall.service.points;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.points.PointsRuleMapper;
import com.mall.model.dto.points.PointsRuleDTO;
import com.mall.model.entity.points.PointsRule;
import com.mall.model.vo.points.PointsRuleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointsRuleService {

    private final PointsRuleMapper pointsRuleMapper;

    public List<PointsRuleVO> list() {
        LambdaQueryWrapper<PointsRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(PointsRule::getSortOrder);
        List<PointsRule> rules = pointsRuleMapper.selectList(wrapper);
        return rules.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(PointsRuleDTO dto) {
        PointsRule rule = new PointsRule();
        BeanUtils.copyProperties(dto, rule);
        rule.setEnabled(dto.getEnabled() != null && dto.getEnabled() ? 1 : 0);
        pointsRuleMapper.insert(rule);
    }

    @Transactional(rollbackFor = Exception.class)
    public void toggleEnabled(Long id) {
        PointsRule rule = pointsRuleMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        rule.setEnabled(rule.getEnabled() == 1 ? 0 : 1);
        pointsRuleMapper.updateById(rule);
    }

    private PointsRuleVO toVO(PointsRule rule) {
        PointsRuleVO vo = new PointsRuleVO();
        vo.setId(rule.getId());
        vo.setRuleType(rule.getRuleType());
        vo.setRuleName(rule.getRuleName());
        vo.setPointsValue(rule.getPointsValue());
        vo.setMultiplier(rule.getMultiplier());
        vo.setDailyLimit(rule.getDailyLimit());
        vo.setEnabled(rule.getEnabled() == 1);
        vo.setSortOrder(rule.getSortOrder());
        return vo;
    }
}
