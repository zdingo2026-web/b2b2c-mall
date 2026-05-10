package com.mall.model.vo.promotion;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GrouponActivityVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String activityName;
    private Long spuId;
    private BigDecimal grouponPrice;
    private Integer limitPerUser;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}
