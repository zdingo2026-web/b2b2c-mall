package com.mall.service.promotion;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.ProductSkuMapper;
import com.mall.dao.mapper.ProductSpuMapper;
import com.mall.dao.mapper.promotion.GroupBuyActivityMapper;
import com.mall.dao.mapper.promotion.GroupBuyMemberMapper;
import com.mall.dao.mapper.promotion.GroupBuyRecordMapper;
import com.mall.model.dto.promotion.GroupBuyCreateDTO;
import com.mall.model.dto.promotion.GroupBuyJoinDTO;
import com.mall.model.entity.ProductSku;
import com.mall.model.entity.ProductSpu;
import com.mall.model.entity.promotion.GroupBuyActivity;
import com.mall.model.entity.promotion.GroupBuyMember;
import com.mall.model.entity.promotion.GroupBuyRecord;
import com.mall.model.vo.promotion.GroupBuyActivityDetailVO;
import com.mall.model.vo.promotion.GroupBuyActivityVO;
import com.mall.model.vo.promotion.GroupBuyDetailVO;
import com.mall.model.vo.promotion.GroupBuyProductVO;
import com.mall.model.vo.promotion.GroupBuyRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupBuyActivityService {

    private final GroupBuyActivityMapper groupBuyActivityMapper;
    private final GroupBuyRecordMapper groupBuyRecordMapper;
    private final GroupBuyMemberMapper groupBuyMemberMapper;
    private final ProductSpuMapper productSpuMapper;
    private final ProductSkuMapper productSkuMapper;
    private final GroupBuyMatchService groupBuyMatchService;

    public PageResult<GroupBuyActivityVO> list(Long tenantId, Integer status, int page, int limit) {
        Page<GroupBuyActivity> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<GroupBuyActivity> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(GroupBuyActivity::getTenantId, tenantId);
        }
        if (status != null) {
            wrapper.eq(GroupBuyActivity::getStatus, status);
        }
        wrapper.orderByDesc(GroupBuyActivity::getCreateTime);
        Page<GroupBuyActivity> result = groupBuyActivityMapper.selectPage(pageParam, wrapper);
        List<GroupBuyActivityVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    public GroupBuyActivityDetailVO detail(Long id) {
        GroupBuyActivity activity = groupBuyActivityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        GroupBuyActivityDetailVO vo = new GroupBuyActivityDetailVO();
        vo.setId(activity.getId());
        vo.setActivityName(activity.getActivityName());
        vo.setSpuId(activity.getSpuId());
        vo.setSkuId(activity.getSkuId());
        vo.setGroupPrice(activity.getGroupPrice());
        vo.setGroupNum(activity.getGroupNum());
        vo.setLimitPerUser(activity.getLimitPerUser());
        vo.setMaxGroups(activity.getMaxGroups());
        vo.setDuration(activity.getDuration());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setStatus(activity.getStatus());

        ProductSpu spu = productSpuMapper.selectById(activity.getSpuId());
        if (spu != null) {
            vo.setSpuName(spu.getProductName());
            vo.setMainImage(spu.getMainImage());
        }
        ProductSku sku = productSkuMapper.selectById(activity.getSkuId());
        if (sku != null) {
            vo.setOriginalPrice(sku.getPrice());
        }
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(Long tenantId, GroupBuyCreateDTO dto) {
        GroupBuyActivity activity = new GroupBuyActivity();
        activity.setActivityName(dto.getActivityName());
        activity.setSpuId(dto.getSpuId());
        activity.setSkuId(dto.getSkuId());
        activity.setGroupPrice(dto.getGroupPrice());
        activity.setGroupNum(dto.getGroupNum());
        activity.setLimitPerUser(dto.getLimitPerUser());
        activity.setMaxGroups(dto.getMaxGroups());
        activity.setDuration(dto.getDuration());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setStatus(0);
        activity.setTenantId(tenantId);
        groupBuyActivityMapper.insert(activity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, GroupBuyCreateDTO dto) {
        GroupBuyActivity activity = groupBuyActivityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        activity.setActivityName(dto.getActivityName());
        activity.setSpuId(dto.getSpuId());
        activity.setSkuId(dto.getSkuId());
        activity.setGroupPrice(dto.getGroupPrice());
        activity.setGroupNum(dto.getGroupNum());
        activity.setLimitPerUser(dto.getLimitPerUser());
        activity.setMaxGroups(dto.getMaxGroups());
        activity.setDuration(dto.getDuration());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        groupBuyActivityMapper.updateById(activity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        GroupBuyActivity activity = groupBuyActivityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        groupBuyActivityMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void startActivity(Long id) {
        GroupBuyActivity activity = groupBuyActivityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        activity.setStatus(1);
        groupBuyActivityMapper.updateById(activity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void endActivity(Long id) {
        GroupBuyActivity activity = groupBuyActivityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        activity.setStatus(2);
        groupBuyActivityMapper.updateById(activity);
    }

    public PageResult<GroupBuyProductVO> products(int page, int limit) {
        Page<GroupBuyActivity> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<GroupBuyActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyActivity::getStatus, 1);
        wrapper.ge(GroupBuyActivity::getEndTime, LocalDateTime.now());
        wrapper.orderByDesc(GroupBuyActivity::getCreateTime);
        Page<GroupBuyActivity> result = groupBuyActivityMapper.selectPage(pageParam, wrapper);

        List<GroupBuyProductVO> voList = result.getRecords().stream().map(activity -> {
            GroupBuyProductVO vo = new GroupBuyProductVO();
            vo.setActivityId(activity.getId());
            vo.setActivityName(activity.getActivityName());
            vo.setSpuId(activity.getSpuId());
            vo.setGroupPrice(activity.getGroupPrice());
            vo.setGroupNum(activity.getGroupNum());
            vo.setEndTime(activity.getEndTime());

            ProductSpu spu = productSpuMapper.selectById(activity.getSpuId());
            if (spu != null) {
                vo.setSpuName(spu.getProductName());
                vo.setMainImage(spu.getMainImage());
            }
            ProductSku sku = productSkuMapper.selectById(activity.getSkuId());
            if (sku != null) {
                vo.setOriginalPrice(sku.getPrice());
            }

            Long joinedGroups = groupBuyRecordMapper.selectCount(
                    new LambdaQueryWrapper<GroupBuyRecord>()
                            .eq(GroupBuyRecord::getActivityId, activity.getId())
                            .ne(GroupBuyRecord::getGroupStatus, 2));
            vo.setJoinedGroups(joinedGroups.intValue());
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    public GroupBuyDetailVO groupDetail(Long activityId) {
        GroupBuyActivity activity = groupBuyActivityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        GroupBuyDetailVO vo = new GroupBuyDetailVO();
        vo.setActivityId(activityId);
        vo.setActivityName(activity.getActivityName());
        vo.setGroupPrice(activity.getGroupPrice());
        vo.setGroupNum(activity.getGroupNum());

        List<GroupBuyRecord> records = groupBuyRecordMapper.selectList(
                new LambdaQueryWrapper<GroupBuyRecord>()
                        .eq(GroupBuyRecord::getActivityId, activityId)
                        .ne(GroupBuyRecord::getGroupStatus, 2)
                        .orderByDesc(GroupBuyRecord::getCreateTime));
        List<GroupBuyRecordVO> recordVOs = records.stream().map(this::toRecordVO).collect(Collectors.toList());
        vo.setRecords(recordVOs);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public GroupBuyRecordVO joinGroup(Long memberId, Long activityId, GroupBuyJoinDTO dto) {
        GroupBuyActivity activity = groupBuyActivityMapper.selectById(activityId);
        if (activity == null || activity.getStatus() != 1) {
            throw new BusinessException("拼团活动不存在或未开始");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            throw new BusinessException("不在活动时间内");
        }

        Long joinedCount = groupBuyMemberMapper.selectCount(
                new LambdaQueryWrapper<GroupBuyMember>()
                        .eq(GroupBuyMember::getMemberId, memberId)
                        .eq(GroupBuyMember::getIsHead, 1));
        if (joinedCount >= activity.getLimitPerUser()) {
            throw new BusinessException("已达到参团上限");
        }

        GroupBuyRecord record = groupBuyMatchService.findOrCreateGroup(activityId, memberId);

        GroupBuyMember member = new GroupBuyMember();
        member.setGroupRecordId(record.getId());
        member.setMemberId(memberId);
        member.setIsHead(record.getCurrentNum() == 1 ? 1 : 0);
        member.setJoinTime(LocalDateTime.now());
        member.setTenantId(activity.getTenantId());
        groupBuyMemberMapper.insert(member);

        record.setCurrentNum(record.getCurrentNum() + 1);
        if (record.getCurrentNum() >= activity.getGroupNum()) {
            groupBuyMatchService.checkGroupCompletion(record.getId());
        } else {
            groupBuyRecordMapper.updateById(record);
        }

        return toRecordVO(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleGroupResult(Long recordId) {
        GroupBuyRecord record = groupBuyRecordMapper.selectById(recordId);
        if (record == null) {
            return;
        }
        GroupBuyActivity activity = groupBuyActivityMapper.selectById(record.getActivityId());
        if (activity == null) {
            return;
        }

        if (record.getCurrentNum() >= activity.getGroupNum()) {
            record.setGroupStatus(1);
            record.setSuccessTime(LocalDateTime.now());
        } else {
            record.setGroupStatus(2);
            record.setFailTime(LocalDateTime.now());
        }
        groupBuyRecordMapper.updateById(record);
    }

    private GroupBuyActivityVO toVO(GroupBuyActivity activity) {
        GroupBuyActivityVO vo = new GroupBuyActivityVO();
        vo.setId(activity.getId());
        vo.setActivityName(activity.getActivityName());
        vo.setSpuId(activity.getSpuId());
        vo.setGroupPrice(activity.getGroupPrice());
        vo.setGroupNum(activity.getGroupNum());
        vo.setLimitPerUser(activity.getLimitPerUser());
        vo.setMaxGroups(activity.getMaxGroups());
        vo.setDuration(activity.getDuration());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setStatus(activity.getStatus());
        return vo;
    }

    private GroupBuyRecordVO toRecordVO(GroupBuyRecord record) {
        GroupBuyRecordVO vo = new GroupBuyRecordVO();
        vo.setId(record.getId());
        vo.setActivityId(record.getActivityId());
        vo.setHeadMemberId(record.getHeadMemberId());
        vo.setGroupStatus(record.getGroupStatus());
        vo.setCurrentNum(record.getCurrentNum());
        vo.setExpireTime(record.getExpireTime());
        return vo;
    }
}
