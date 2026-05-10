package com.mall.service.distribution;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.MemberMapper;
import com.mall.dao.mapper.distribution.DistributionConfigMapper;
import com.mall.dao.mapper.distribution.DistributionRelationMapper;
import com.mall.dao.mapper.distribution.DistributorMapper;
import com.mall.model.dto.distribution.DistributorApplyDTO;
import com.mall.model.entity.Member;
import com.mall.model.entity.distribution.DistributionConfig;
import com.mall.model.entity.distribution.DistributionRelation;
import com.mall.model.entity.distribution.Distributor;
import com.mall.model.vo.distribution.DistributionCenterVO;
import com.mall.model.vo.distribution.DistributorVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributorService {

    private final DistributorMapper distributorMapper;
    private final DistributionRelationMapper distributionRelationMapper;
    private final MemberMapper memberMapper;
    private final DistributionConfigMapper distributionConfigMapper;

    @Transactional(rollbackFor = Exception.class)
    public void apply(Long memberId, DistributorApplyDTO dto) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        Long count = distributorMapper.selectCount(
                new LambdaQueryWrapper<Distributor>()
                        .eq(Distributor::getMemberId, memberId)
                        .ne(Distributor::getStatus, 2));
        if (count > 0) {
            throw new BusinessException("已申请过分销员");
        }
        DistributionConfig config = distributionConfigMapper.selectOne(
                new LambdaQueryWrapper<DistributionConfig>().last("LIMIT 1"));
        if (config == null || config.getEnabled() != 1) {
            throw new BusinessException("分销功能未开启");
        }

        Distributor distributor = new Distributor();
        distributor.setMemberId(memberId);
        distributor.setRealName(dto.getRealName());
        distributor.setPhone(dto.getPhone());
        distributor.setStatus(config.getAutoAudit() != null && config.getAutoAudit() == 1 ? 1 : 0);
        distributor.setTotalCommission(BigDecimal.ZERO);
        distributor.setAvailableCommission(BigDecimal.ZERO);
        distributor.setFrozenCommission(BigDecimal.ZERO);
        distributorMapper.insert(distributor);

        if (distributor.getStatus() == 1) {
            distributor.setAuditTime(java.time.LocalDateTime.now());
            distributorMapper.updateById(distributor);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void audit(Long distributorId, Integer status, String rejectReason) {
        Distributor distributor = distributorMapper.selectById(distributorId);
        if (distributor == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (distributor.getStatus() != 0) {
            throw new BusinessException("该分销员不在待审核状态");
        }
        distributor.setStatus(status);
        distributor.setAuditTime(java.time.LocalDateTime.now());
        if (status == 2 && rejectReason != null) {
            distributor.setRejectReason(rejectReason);
        }
        distributorMapper.updateById(distributor);
    }

    public DistributorVO getDistributor(Long memberId) {
        Distributor distributor = distributorMapper.selectOne(
                new LambdaQueryWrapper<Distributor>().eq(Distributor::getMemberId, memberId));
        if (distributor == null) {
            return null;
        }
        return toDistributorVO(distributor);
    }

    public DistributionCenterVO center(Long memberId) {
        DistributionCenterVO vo = new DistributionCenterVO();
        Distributor distributor = distributorMapper.selectOne(
                new LambdaQueryWrapper<Distributor>().eq(Distributor::getMemberId, memberId));
        if (distributor == null) {
            vo.setIsDistributor(false);
            vo.setStatus(0);
            vo.setTotalCommission(BigDecimal.ZERO);
            vo.setAvailableCommission(BigDecimal.ZERO);
            vo.setFrozenCommission(BigDecimal.ZERO);
            vo.setTeamSize(0);
            return vo;
        }
        vo.setIsDistributor(true);
        vo.setStatus(distributor.getStatus());
        vo.setTotalCommission(distributor.getTotalCommission());
        vo.setAvailableCommission(distributor.getAvailableCommission());
        vo.setFrozenCommission(distributor.getFrozenCommission());

        Long teamSize = distributorMapper.selectCount(
                new LambdaQueryWrapper<Distributor>().eq(Distributor::getParentId, distributor.getId()));
        vo.setTeamSize(teamSize.intValue());
        return vo;
    }

    public PageResult<DistributorVO> list(String keyword, Integer status, int page, int limit) {
        Page<Distributor> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<Distributor> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Distributor::getRealName, keyword)
                    .or().like(Distributor::getPhone, keyword));
        }
        if (status != null) {
            wrapper.eq(Distributor::getStatus, status);
        }
        wrapper.orderByDesc(Distributor::getCreateTime);

        Page<Distributor> result = distributorMapper.selectPage(pageParam, wrapper);
        List<DistributorVO> voList = result.getRecords().stream()
                .map(this::toDistributorVO)
                .collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    public PageResult<DistributorVO> teamList(Long parentId, int page, int limit) {
        Page<Distributor> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<Distributor> wrapper = new LambdaQueryWrapper<Distributor>()
                .eq(Distributor::getParentId, parentId)
                .orderByDesc(Distributor::getCreateTime);

        Page<Distributor> result = distributorMapper.selectPage(pageParam, wrapper);
        List<DistributorVO> voList = result.getRecords().stream()
                .map(this::toDistributorVO)
                .collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    private DistributorVO toDistributorVO(Distributor d) {
        DistributorVO vo = new DistributorVO();
        vo.setId(d.getId());
        vo.setMemberId(d.getMemberId());
        vo.setRealName(d.getRealName());
        vo.setPhone(d.getPhone());
        vo.setStatus(d.getStatus());
        vo.setRejectReason(d.getRejectReason());
        vo.setTotalCommission(d.getTotalCommission());
        vo.setAvailableCommission(d.getAvailableCommission());
        vo.setFrozenCommission(d.getFrozenCommission());
        vo.setAuditTime(d.getAuditTime());
        vo.setCreateTime(d.getCreateTime());
        return vo;
    }
}
