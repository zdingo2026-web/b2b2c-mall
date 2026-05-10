package com.mall.service.member;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.MemberMapper;
import com.mall.dao.mapper.member.MemberRedPacketMapper;
import com.mall.dao.mapper.member.RedPacketBatchMapper;
import com.mall.model.entity.Member;
import com.mall.model.entity.member.MemberRedPacket;
import com.mall.model.entity.member.RedPacketBatch;
import com.mall.model.vo.member.MemberRedPacketVO;
import com.mall.model.vo.member.RedPacketBatchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedPacketService {

    private final RedPacketBatchMapper redPacketBatchMapper;
    private final MemberRedPacketMapper memberRedPacketMapper;
    private final MemberMapper memberMapper;

    public PageResult<RedPacketBatchVO> batchList(Long tenantId, Integer status, int page, int limit) {
        Page<RedPacketBatch> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<RedPacketBatch> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(RedPacketBatch::getTenantId, tenantId);
        }
        if (status != null) {
            wrapper.eq(RedPacketBatch::getStatus, status);
        }
        wrapper.orderByDesc(RedPacketBatch::getCreateTime);

        Page<RedPacketBatch> result = redPacketBatchMapper.selectPage(pageParam, wrapper);
        List<RedPacketBatchVO> voList = result.getRecords().stream().map(b -> {
            RedPacketBatchVO vo = new RedPacketBatchVO();
            BeanUtils.copyProperties(b, vo);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createBatch(Long tenantId, RedPacketBatch batch) {
        batch.setTenantId(tenantId);
        batch.setClaimedCount(0);
        batch.setUsedCount(0);
        batch.setStatus(0);
        redPacketBatchMapper.insert(batch);
    }

    @Transactional(rollbackFor = Exception.class)
    public void sendBatch(Long batchId) {
        RedPacketBatch batch = redPacketBatchMapper.selectById(batchId);
        if (batch == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (batch.getStatus() != 0) {
            throw new BusinessException("红包批次状态异常");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validStart = now;
        LocalDateTime validEnd;
        if (batch.getValidType() == 1) {
            validStart = batch.getValidStartTime();
            validEnd = batch.getValidEndTime();
        } else {
            validEnd = now.plusDays(batch.getValidDays() != null ? batch.getValidDays() : 30);
        }

        List<MemberRedPacket> packets = new ArrayList<>();
        int totalCount = batch.getTotalCount() != null ? batch.getTotalCount() : 0;
        for (int i = 0; i < totalCount; i++) {
            MemberRedPacket packet = new MemberRedPacket();
            packet.setTenantId(batch.getTenantId());
            packet.setBatchId(batchId);
            packet.setFaceValue(batch.getFaceValue());
            packet.setMinAmount(batch.getMinAmount());
            packet.setSourceType(batch.getSendType());
            packet.setStatus(0);
            packet.setValidStartTime(validStart);
            packet.setValidEndTime(validEnd);
            packets.add(packet);
        }

        for (MemberRedPacket packet : packets) {
            memberRedPacketMapper.insert(packet);
        }

        batch.setStatus(1);
        redPacketBatchMapper.updateById(batch);
    }

    @Transactional(rollbackFor = Exception.class)
    public MemberRedPacketVO claimRedPacket(Long memberId, Long batchId) {
        RedPacketBatch batch = redPacketBatchMapper.selectById(batchId);
        if (batch == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (batch.getStatus() != 1) {
            throw new BusinessException("红包未发放或已结束");
        }

        Long claimed = memberRedPacketMapper.selectCount(
                new LambdaQueryWrapper<MemberRedPacket>()
                        .eq(MemberRedPacket::getBatchId, batchId)
                        .eq(MemberRedPacket::getMemberId, memberId));
        if (claimed > 0) {
            throw new BusinessException("已领取过该红包");
        }

        Long available = memberRedPacketMapper.selectCount(
                new LambdaQueryWrapper<MemberRedPacket>()
                        .eq(MemberRedPacket::getBatchId, batchId)
                        .eq(MemberRedPacket::getStatus, 0)
                        .isNull(MemberRedPacket::getMemberId));
        if (available <= 0) {
            throw new BusinessException("红包已领完");
        }

        MemberRedPacket packet = memberRedPacketMapper.selectOne(
                new LambdaQueryWrapper<MemberRedPacket>()
                        .eq(MemberRedPacket::getBatchId, batchId)
                        .eq(MemberRedPacket::getStatus, 0)
                        .isNull(MemberRedPacket::getMemberId)
                        .last("LIMIT 1"));
        if (packet == null) {
            throw new BusinessException("红包已领完");
        }

        packet.setMemberId(memberId);
        packet.setStatus(1);
        memberRedPacketMapper.updateById(packet);

        batch.setClaimedCount((batch.getClaimedCount() != null ? batch.getClaimedCount() : 0) + 1);
        redPacketBatchMapper.updateById(batch);

        Member member = memberMapper.selectById(memberId);
        if (member != null) {
            BigDecimal newBalance = (member.getRedPacketBalance() != null ? member.getRedPacketBalance() : BigDecimal.ZERO)
                    .add(packet.getFaceValue());
            member.setRedPacketBalance(newBalance);
            memberMapper.updateById(member);
        }

        MemberRedPacketVO vo = new MemberRedPacketVO();
        BeanUtils.copyProperties(packet, vo);
        return vo;
    }

    public PageResult<MemberRedPacketVO> memberRedPackets(Long memberId, Integer status, int page, int limit) {
        Page<MemberRedPacket> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<MemberRedPacket> wrapper = new LambdaQueryWrapper<>();
        if (memberId != null) {
            wrapper.eq(MemberRedPacket::getMemberId, memberId);
        }
        if (status != null) {
            wrapper.eq(MemberRedPacket::getStatus, status);
        }
        wrapper.orderByDesc(MemberRedPacket::getCreateTime);

        Page<MemberRedPacket> result = memberRedPacketMapper.selectPage(pageParam, wrapper);
        List<MemberRedPacketVO> voList = result.getRecords().stream().map(p -> {
            MemberRedPacketVO vo = new MemberRedPacketVO();
            BeanUtils.copyProperties(p, vo);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), page, limit);
    }
}
