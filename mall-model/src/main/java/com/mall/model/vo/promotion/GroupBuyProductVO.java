package com.mall.model.vo.promotion;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GroupBuyProductVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long activityId;
    private String activityName;
    private Long spuId;
    private String spuName;
    private String mainImage;
    private BigDecimal originalPrice;
    private BigDecimal groupPrice;
    private Integer groupNum;
    private Integer joinedGroups;
    private LocalDateTime endTime;
}
