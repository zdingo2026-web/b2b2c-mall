package com.mall.model.vo.promotion;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class GroupBuyRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long activityId;
    private Long headMemberId;
    private String headMemberNickname;
    private String headMemberAvatar;
    private Integer groupStatus;
    private Integer currentNum;
    private Integer groupNum;
    private LocalDateTime expireTime;
}
