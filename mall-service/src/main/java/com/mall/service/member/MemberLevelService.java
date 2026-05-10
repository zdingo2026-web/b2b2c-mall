package com.mall.service.member;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.MemberMapper;
import com.mall.dao.mapper.member.MemberLevelMapper;
import com.mall.model.dto.member.MemberLevelDTO;
import com.mall.model.entity.Member;
import com.mall.model.entity.member.MemberLevel;
import com.mall.model.vo.member.MemberLevelVO;
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
public class MemberLevelService {

    private final MemberLevelMapper memberLevelMapper;
    private final MemberMapper memberMapper;

    public List<MemberLevelVO> list() {
        List<MemberLevel> levels = memberLevelMapper.selectList(
                new LambdaQueryWrapper<MemberLevel>()
                        .orderByAsc(MemberLevel::getSortWeight)
                        .orderByAsc(MemberLevel::getRequiredGrowth));
        return levels.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(MemberLevelDTO dto) {
        MemberLevel level = new MemberLevel();
        BeanUtils.copyProperties(dto, level);
        memberLevelMapper.insert(level);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, MemberLevelDTO dto) {
        MemberLevel level = memberLevelMapper.selectById(id);
        if (level == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (dto.getLevelName() != null) {
            level.setLevelName(dto.getLevelName());
        }
        if (dto.getLevelIcon() != null) {
            level.setLevelIcon(dto.getLevelIcon());
        }
        if (dto.getRequiredGrowth() != null) {
            level.setRequiredGrowth(dto.getRequiredGrowth());
        }
        if (dto.getPointsMultiplier() != null) {
            level.setPointsMultiplier(dto.getPointsMultiplier());
        }
        if (dto.getExclusiveDiscount() != null) {
            level.setExclusiveDiscount(dto.getExclusiveDiscount());
        }
        if (dto.getSortWeight() != null) {
            level.setSortWeight(dto.getSortWeight());
        }
        memberLevelMapper.updateById(level);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        MemberLevel level = memberLevelMapper.selectById(id);
        if (level == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        Long count = memberMapper.selectCount(
                new LambdaQueryWrapper<Member>()
                        .eq(Member::getLevelId, id));
        if (count > 0) {
            throw new BusinessException("该等级下存在会员，无法删除");
        }
        memberLevelMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void recalculateLevels() {
        List<MemberLevel> levels = memberLevelMapper.selectList(
                new LambdaQueryWrapper<MemberLevel>()
                        .orderByAsc(MemberLevel::getRequiredGrowth));
        if (levels.isEmpty()) {
            return;
        }

        List<Member> members = memberMapper.selectList(
                new LambdaQueryWrapper<Member>()
                        .isNotNull(Member::getPoints));
        for (Member member : members) {
            int points = member.getPoints() != null ? member.getPoints() : 0;
            MemberLevel matched = null;
            for (MemberLevel level : levels) {
                if (points >= level.getRequiredGrowth()) {
                    matched = level;
                }
            }
            if (matched != null && !matched.getId().equals(member.getLevelId())) {
                member.setLevelId(matched.getId());
                memberMapper.updateById(member);
            }
        }
    }

    private MemberLevelVO toVO(MemberLevel level) {
        MemberLevelVO vo = new MemberLevelVO();
        BeanUtils.copyProperties(level, vo);
        return vo;
    }
}
