package com.mall.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.MemberMessageMapper;
import com.mall.model.entity.MemberMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Member message service.
 * Handles message listing, unread count, and mark-read operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberMessageService {

    private final MemberMessageMapper memberMessageMapper;

    /**
     * Get paginated message list for a member.
     *
     * @param memberId member ID
     * @param msgType  message type filter (null for all)
     * @param page     page number
     * @param limit    page size
     * @return paginated message list
     */
    public PageResult<MemberMessage> getMessageList(Long memberId, Integer msgType, int page, int limit) {
        Page<MemberMessage> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<MemberMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberMessage::getMemberId, memberId);
        if (msgType != null) {
            wrapper.eq(MemberMessage::getMsgType, msgType);
        }
        wrapper.orderByDesc(MemberMessage::getCreateTime);

        Page<MemberMessage> result = memberMessageMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, limit);
    }

    /**
     * Count unread messages for a member.
     *
     * @param memberId member ID
     * @return unread message count
     */
    public long getUnreadCount(Long memberId) {
        return memberMessageMapper.selectCount(
                new LambdaQueryWrapper<MemberMessage>()
                        .eq(MemberMessage::getMemberId, memberId)
                        .eq(MemberMessage::getIsRead, 0));
    }

    /**
     * Mark a single message as read (verify ownership).
     *
     * @param memberId   member ID
     * @param messageId  message ID
     */
    public void markRead(Long memberId, Long messageId) {
        MemberMessage message = memberMessageMapper.selectById(messageId);
        if (message == null || !message.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (message.getIsRead() == 0) {
            message.setIsRead(1);
            memberMessageMapper.updateById(message);
        }
    }

    /**
     * Mark all messages as read for a member.
     *
     * @param memberId member ID
     */
    public void markAllRead(Long memberId) {
        MemberMessage update = new MemberMessage();
        update.setIsRead(1);
        memberMessageMapper.update(update,
                new LambdaUpdateWrapper<MemberMessage>()
                        .eq(MemberMessage::getMemberId, memberId)
                        .eq(MemberMessage::getIsRead, 0));
    }
}
