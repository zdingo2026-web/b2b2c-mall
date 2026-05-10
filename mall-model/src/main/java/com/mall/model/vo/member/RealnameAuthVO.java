package com.mall.model.vo.member;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RealnameAuthVO {
    private Long id;
    private Long memberId;
    private String realName;
    private Integer idCardType;
    private Integer status;
    private String rejectReason;
    private LocalDateTime auditTime;
}
