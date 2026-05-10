package com.mall.model.vo.points;

import lombok.Data;

import java.io.Serializable;

@Data
public class PointsAccountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long memberId;
    private Integer balance;
    private Integer totalEarned;
    private Integer totalSpent;
}
