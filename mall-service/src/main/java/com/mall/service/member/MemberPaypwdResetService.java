package com.mall.service.member;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.MemberMapper;
import com.mall.dao.mapper.member.MemberPaypwdResetMapper;
import com.mall.model.entity.Member;
import com.mall.model.entity.member.MemberPaypwdReset;
import com.mall.model.vo.member.PayPasswordResetVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberPaypwdResetService {

    private final MemberPaypwdResetMapper paypwdResetMapper;
    private final MemberMapper memberMapper;

    @Transactional(rollbackFor = Exception.class)
    public void requestReset(Long memberId, String resetReason) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        Long count = paypwdResetMapper.selectCount(
                new LambdaQueryWrapper<MemberPaypwdReset>()
                        .eq(MemberPaypwdReset::getMemberId, memberId)
                        .eq(MemberPaypwdReset::getStatus, 0));
        if (count > 0) {
            throw new BusinessException("已有待处理的支付密码重置申请");
        }

        MemberPaypwdReset reset = new MemberPaypwdReset();
        reset.setMemberId(memberId);
        reset.setResetReason(resetReason);
        reset.setStatus(0);
        reset.setNotifySent(0);
        paypwdResetMapper.insert(reset);
    }

    public PageResult<PayPasswordResetVO> list(Integer status, int page, int limit) {
        Page<MemberPaypwdReset> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<MemberPaypwdReset> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(MemberPaypwdReset::getStatus, status);
        }
        wrapper.orderByDesc(MemberPaypwdReset::getCreateTime);

        Page<MemberPaypwdReset> result = paypwdResetMapper.selectPage(pageParam, wrapper);

        List<Long> memberIds = result.getRecords().stream()
                .map(MemberPaypwdReset::getMemberId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Member> memberMap = memberIds.isEmpty() ? Collections.emptyMap() :
                memberMapper.selectBatchIds(memberIds).stream()
                        .collect(Collectors.toMap(Member::getId, m -> m, (a, b) -> a));

        List<PayPasswordResetVO> voList = result.getRecords().stream().map(r -> {
            PayPasswordResetVO vo = new PayPasswordResetVO();
            BeanUtils.copyProperties(r, vo);
            vo.setNotifySent(r.getNotifySent() != null && r.getNotifySent() == 1);
            Member member = memberMap.get(r.getMemberId());
            if (member != null) {
                vo.setMemberNickname(member.getNickname());
                vo.setMemberPhone(member.getPhone());
            }
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, Long operatorId) {
        MemberPaypwdReset reset = paypwdResetMapper.selectById(id);
        if (reset == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (reset.getStatus() != 0) {
            throw new BusinessException("该申请已处理");
        }

        reset.setStatus(1);
        reset.setOperatorId(operatorId);
        paypwdResetMapper.updateById(reset);

        Member member = memberMapper.selectById(reset.getMemberId());
        if (member != null) {
            member.setHasPayPassword(0);
            member.setPassword(null);
            memberMapper.updateById(member);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, Long operatorId, String reason) {
        MemberPaypwdReset reset = paypwdResetMapper.selectById(id);
        if (reset == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (reset.getStatus() != 0) {
            throw new BusinessException("该申请已处理");
        }

        reset.setStatus(2);
        reset.setOperatorId(operatorId);
        reset.setRejectReason(reason);
        paypwdResetMapper.updateById(reset);
    }
}
