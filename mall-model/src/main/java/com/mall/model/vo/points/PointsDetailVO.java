package com.mall.model.vo.points;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PointsDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer bizType;
    private String bizId;
    private Integer changeAmount;
    private Integer changeType;
    private Integer balanceAfter;
    private String remark;
    private LocalDateTime createTime;
}
