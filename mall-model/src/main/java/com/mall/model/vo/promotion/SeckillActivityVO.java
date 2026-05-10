package com.mall.model.vo.promotion;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SeckillActivityVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String activityName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer paymentTimeout;
    private Integer status;
    private LocalDateTime createTime;
}
