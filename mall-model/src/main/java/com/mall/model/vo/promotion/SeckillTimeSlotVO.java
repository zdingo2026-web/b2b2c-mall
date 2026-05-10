package com.mall.model.vo.promotion;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SeckillTimeSlotVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String timeSlot;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}
