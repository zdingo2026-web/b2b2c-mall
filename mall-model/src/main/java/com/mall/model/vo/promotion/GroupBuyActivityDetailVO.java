package com.mall.model.vo.promotion;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GroupBuyActivityDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String activityName;
    private Long spuId;
    private Long skuId;
    private BigDecimal groupPrice;
    private Integer groupNum;
    private Integer limitPerUser;
    private Integer maxGroups;
    private Integer duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private String spuName;
    private String mainImage;
    private BigDecimal originalPrice;
}
