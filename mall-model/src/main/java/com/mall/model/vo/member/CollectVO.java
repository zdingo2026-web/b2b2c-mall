package com.mall.model.vo.member;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CollectVO {
    private Long id;
    private Integer collectType;
    private Long targetId;
    private String targetName;
    private String targetImage;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Boolean priceDecreased;
    private LocalDateTime createTime;
}
