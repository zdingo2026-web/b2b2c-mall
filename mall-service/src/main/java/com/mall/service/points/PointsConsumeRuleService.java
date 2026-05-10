package com.mall.service.points;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.dao.mapper.points.PointsConsumeRuleMapper;
import com.mall.model.dto.points.PointsConsumeRuleDTO;
import com.mall.model.entity.points.PointsConsumeRule;
import com.mall.model.vo.points.PointsConsumeRuleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointsConsumeRuleService {

    private final PointsConsumeRuleMapper pointsConsumeRuleMapper;

    public PointsConsumeRuleVO getConfig() {
        LambdaQueryWrapper<PointsConsumeRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.last("LIMIT 1");
        PointsConsumeRule rule = pointsConsumeRuleMapper.selectOne(wrapper);
        if (rule == null) {
            return new PointsConsumeRuleVO();
        }
        PointsConsumeRuleVO vo = new PointsConsumeRuleVO();
        BeanUtils.copyProperties(rule, vo);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(PointsConsumeRuleDTO dto) {
        LambdaQueryWrapper<PointsConsumeRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.last("LIMIT 1");
        PointsConsumeRule existing = pointsConsumeRuleMapper.selectOne(wrapper);
        if (existing == null) {
            PointsConsumeRule rule = new PointsConsumeRule();
            BeanUtils.copyProperties(dto, rule);
            pointsConsumeRuleMapper.insert(rule);
        } else {
            BeanUtils.copyProperties(dto, existing);
            pointsConsumeRuleMapper.updateById(existing);
        }
    }
}
