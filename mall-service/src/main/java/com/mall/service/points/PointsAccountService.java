package com.mall.service.points;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.points.PointsAccountMapper;
import com.mall.dao.mapper.points.PointsDetailMapper;
import com.mall.model.entity.points.PointsAccount;
import com.mall.model.entity.points.PointsDetail;
import com.mall.model.vo.points.PointsAccountVO;
import com.mall.model.vo.points.PointsDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointsAccountService {

    private final PointsAccountMapper pointsAccountMapper;
    private final PointsDetailMapper pointsDetailMapper;

    public PointsAccountVO getAccount(Long memberId) {
        PointsAccount account = pointsAccountMapper.selectOne(
                new LambdaQueryWrapper<PointsAccount>().eq(PointsAccount::getMemberId, memberId));
        if (account == null) {
            account = new PointsAccount();
            account.setMemberId(memberId);
            account.setBalance(0);
            account.setTotalEarned(0);
            account.setTotalSpent(0);
            pointsAccountMapper.insert(account);
        }
        PointsAccountVO vo = new PointsAccountVO();
        BeanUtils.copyProperties(account, vo);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void earnPoints(Long memberId, Integer bizType, String bizId, int points, String remark) {
        PointsAccount account = pointsAccountMapper.selectOne(
                new LambdaQueryWrapper<PointsAccount>().eq(PointsAccount::getMemberId, memberId));
        if (account == null) {
            account = new PointsAccount();
            account.setMemberId(memberId);
            account.setBalance(0);
            account.setTotalEarned(0);
            account.setTotalSpent(0);
            pointsAccountMapper.insert(account);
        }
        account.setBalance(account.getBalance() + points);
        account.setTotalEarned(account.getTotalEarned() + points);
        pointsAccountMapper.updateById(account);

        PointsDetail detail = new PointsDetail();
        detail.setMemberId(memberId);
        detail.setBizType(bizType);
        detail.setBizId(bizId);
        detail.setChangeAmount(points);
        detail.setChangeType(1);
        detail.setBalanceAfter(account.getBalance());
        detail.setRemark(remark);
        detail.setExpired(0);
        pointsDetailMapper.insert(detail);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deductPoints(Long memberId, Integer bizType, String bizId, int points, String remark) {
        PointsAccount account = pointsAccountMapper.selectOne(
                new LambdaQueryWrapper<PointsAccount>().eq(PointsAccount::getMemberId, memberId));
        if (account == null || account.getBalance() < points) {
            return false;
        }
        account.setBalance(account.getBalance() - points);
        account.setTotalSpent(account.getTotalSpent() + points);
        pointsAccountMapper.updateById(account);

        PointsDetail detail = new PointsDetail();
        detail.setMemberId(memberId);
        detail.setBizType(bizType);
        detail.setBizId(bizId);
        detail.setChangeAmount(-points);
        detail.setChangeType(2);
        detail.setBalanceAfter(account.getBalance());
        detail.setRemark(remark);
        detail.setExpired(0);
        pointsDetailMapper.insert(detail);
        return true;
    }

    public PageResult<PointsDetailVO> details(Long memberId, Integer bizType, int page, int limit) {
        Page<PointsDetail> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<PointsDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsDetail::getMemberId, memberId);
        if (bizType != null) {
            wrapper.eq(PointsDetail::getBizType, bizType);
        }
        wrapper.orderByDesc(PointsDetail::getCreateTime);
        Page<PointsDetail> result = pointsDetailMapper.selectPage(pageParam, wrapper);
        List<PointsDetailVO> voList = result.getRecords().stream().map(d -> {
            PointsDetailVO vo = new PointsDetailVO();
            vo.setId(d.getId());
            vo.setBizType(d.getBizType());
            vo.setBizId(d.getBizId());
            vo.setChangeAmount(d.getChangeAmount());
            vo.setChangeType(d.getChangeType());
            vo.setBalanceAfter(d.getBalanceAfter());
            vo.setRemark(d.getRemark());
            vo.setCreateTime(d.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, limit);
    }
}
