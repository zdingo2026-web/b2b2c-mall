package com.mall.model.vo.promotion;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GroupBuyDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long activityId;
    private String activityName;
    private BigDecimal groupPrice;
    private Integer groupNum;
    private List<GroupBuyRecordVO> records;
}
