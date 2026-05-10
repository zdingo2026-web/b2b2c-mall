package com.mall.model.vo.member;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PayPasswordResetVO {
    private Long id;
    private Long memberId;
    private String memberNickname;
    private String memberPhone;
    private String resetReason;
    private Boolean notifySent;
    private LocalDateTime createTime;
}
