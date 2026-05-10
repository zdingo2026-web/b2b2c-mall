package com.mall.service.promotion;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.dao.mapper.promotion.GroupBuyMemberMapper;
import com.mall.dao.mapper.promotion.GroupBuyRecordMapper;
import com.mall.model.entity.promotion.GroupBuyActivity;
import com.mall.model.entity.promotion.GroupBuyMember;
import com.mall.model.entity.promotion.GroupBuyRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupBuyMatchService {

    private final GroupBuyRecordMapper groupBuyRecordMapper;
    private final GroupBuyMemberMapper groupBuyMemberMapper;

    public GroupBuyRecord findOrCreateGroup(Long activityId, Long memberId) {
        List<GroupBuyRecord> openRecords = groupBuyRecordMapper.selectList(
                new LambdaQueryWrapper<GroupBuyRecord>()
                        .eq(GroupBuyRecord::getActivityId, activityId)
                        .eq(GroupBuyRecord::getGroupStatus, 0)
                        .gt(GroupBuyRecord::getExpireTime, LocalDateTime.now())
                        .orderByAsc(GroupBuyRecord::getCreateTime)
                        .last("LIMIT 1"));

        if (!openRecords.isEmpty()) {
            GroupBuyRecord record = openRecords.get(0);
            boolean alreadyJoined = groupBuyMemberMapper.selectCount(
                    new LambdaQueryWrapper<GroupBuyMember>()
                            .eq(GroupBuyMember::getGroupRecordId, record.getId())
                            .eq(GroupBuyMember::getMemberId, memberId)) > 0;
            if (!alreadyJoined) {
                return record;
            }
        }

        GroupBuyRecord newRecord = new GroupBuyRecord();
        newRecord.setActivityId(activityId);
        newRecord.setHeadMemberId(memberId);
        newRecord.setGroupStatus(0);
        newRecord.setCurrentNum(0);
        newRecord.setExpireTime(LocalDateTime.now().plusHours(24));
        groupBuyRecordMapper.insert(newRecord);

        GroupBuyMember headMember = new GroupBuyMember();
        headMember.setGroupRecordId(newRecord.getId());
        headMember.setMemberId(memberId);
        headMember.setIsHead(1);
        headMember.setJoinTime(LocalDateTime.now());
        groupBuyMemberMapper.insert(headMember);

        return newRecord;
    }

    public void checkGroupCompletion(Long recordId) {
        GroupBuyRecord record = groupBuyRecordMapper.selectById(recordId);
        if (record == null) {
            return;
        }
        List<GroupBuyMember> members = groupBuyMemberMapper.selectList(
                new LambdaQueryWrapper<GroupBuyMember>()
                        .eq(GroupBuyMember::getGroupRecordId, recordId));
        record.setCurrentNum(members.size());

        GroupBuyActivity activity = new GroupBuyActivity();
        activity.setId(record.getActivityId());
        if (record.getCurrentNum() >= activity.getGroupNum()) {
            record.setGroupStatus(1);
            record.setSuccessTime(LocalDateTime.now());
        }
        groupBuyRecordMapper.updateById(record);
    }
}
