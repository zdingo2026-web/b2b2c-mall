package com.mall.service.member;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.MemberMapper;
import com.mall.dao.mapper.member.MemberRealnameAuthMapper;
import com.mall.model.dto.member.RealnameAuthDTO;
import com.mall.model.entity.Member;
import com.mall.model.entity.member.MemberRealnameAuth;
import com.mall.model.vo.member.RealnameAuthVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberRealnameAuthService {

    private final MemberRealnameAuthMapper realnameAuthMapper;
    private final MemberMapper memberMapper;

    @Transactional(rollbackFor = Exception.class)
    public void submitAuth(Long memberId, RealnameAuthDTO dto) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (member.getRealnameStatus() != null && member.getRealnameStatus() == 2) {
            throw new BusinessException("已完成实名认证，无需重复提交");
        }

        Long count = realnameAuthMapper.selectCount(
                new LambdaQueryWrapper<MemberRealnameAuth>()
                        .eq(MemberRealnameAuth::getMemberId, memberId)
                        .eq(MemberRealnameAuth::getStatus, 0));
        if (count > 0) {
            throw new BusinessException("已有待审核的实名认证申请");
        }

        MemberRealnameAuth auth = new MemberRealnameAuth();
        auth.setMemberId(memberId);
        auth.setRealName(dto.getRealName());
        auth.setIdCardType(dto.getIdCardType());
        auth.setIdCardNo(dto.getIdCardNo());
        auth.setIdCardFront(dto.getIdCardFront());
        auth.setIdCardBack(dto.getIdCardBack());
        auth.setStatus(0);
        realnameAuthMapper.insert(auth);

        member.setRealnameStatus(1);
        memberMapper.updateById(member);
    }

    @Transactional(rollbackFor = Exception.class)
    public void audit(Long authId, Integer status, String rejectReason, Long auditorId) {
        MemberRealnameAuth auth = realnameAuthMapper.selectById(authId);
        if (auth == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (auth.getStatus() != 0) {
            throw new BusinessException("该认证已审核");
        }

        auth.setStatus(status);
        auth.setAuditorId(auditorId);
        auth.setAuditTime(LocalDateTime.now());
        if (status == 3) {
            auth.setRejectReason(rejectReason);
        }
        realnameAuthMapper.updateById(auth);

        Member member = memberMapper.selectById(auth.getMemberId());
        if (member != null) {
            member.setRealnameStatus(status == 2 ? 2 : 0);
            memberMapper.updateById(member);
        }
    }

    public RealnameAuthVO getAuthStatus(Long memberId) {
        MemberRealnameAuth auth = realnameAuthMapper.selectOne(
                new LambdaQueryWrapper<MemberRealnameAuth>()
                        .eq(MemberRealnameAuth::getMemberId, memberId)
                        .orderByDesc(MemberRealnameAuth::getCreateTime)
                        .last("LIMIT 1"));

        RealnameAuthVO vo = new RealnameAuthVO();
        if (auth == null) {
            vo.setMemberId(memberId);
            vo.setStatus(0);
            return vo;
        }
        BeanUtils.copyProperties(auth, vo);
        return vo;
    }
}
