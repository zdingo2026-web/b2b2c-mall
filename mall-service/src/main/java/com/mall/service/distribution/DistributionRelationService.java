package com.mall.service.distribution;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.distribution.DistributionRelationMapper;
import com.mall.dao.mapper.distribution.DistributorMapper;
import com.mall.model.entity.distribution.DistributionRelation;
import com.mall.model.entity.distribution.Distributor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributionRelationService {

    private final DistributionRelationMapper distributionRelationMapper;
    private final DistributorMapper distributorMapper;

    @Transactional(rollbackFor = Exception.class)
    public void bindRelation(Long memberId, Long inviterId) {
        DistributionRelation existing = distributionRelationMapper.selectOne(
                new LambdaQueryWrapper<DistributionRelation>()
                        .eq(DistributionRelation::getMemberId, memberId));
        if (existing != null) {
            return;
        }

        Distributor inviter = distributorMapper.selectById(inviterId);
        if (inviter == null || inviter.getStatus() != 1) {
            throw new BusinessException("邀请人不是有效分销员");
        }

        DistributionRelation relation = new DistributionRelation();
        relation.setMemberId(memberId);
        relation.setParentLevel1(inviter.getId());

        DistributionRelation inviterRelation = distributionRelationMapper.selectOne(
                new LambdaQueryWrapper<DistributionRelation>()
                        .eq(DistributionRelation::getMemberId, inviter.getMemberId()));
        if (inviterRelation != null) {
            if (inviterRelation.getParentLevel1() != null) {
                relation.setParentLevel2(inviterRelation.getParentLevel1());
            }
            if (inviterRelation.getParentLevel2() != null) {
                relation.setParentLevel3(inviterRelation.getParentLevel2());
            }
        }

        distributionRelationMapper.insert(relation);
    }

    public DistributionRelation getRelation(Long memberId) {
        return distributionRelationMapper.selectOne(
                new LambdaQueryWrapper<DistributionRelation>()
                        .eq(DistributionRelation::getMemberId, memberId));
    }

    public Distributor getInviter(Long memberId) {
        DistributionRelation relation = getRelation(memberId);
        if (relation == null || relation.getParentLevel1() == null) {
            return null;
        }
        return distributorMapper.selectById(relation.getParentLevel1());
    }
}
