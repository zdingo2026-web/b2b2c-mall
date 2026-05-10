package com.mall.model.vo.points;

import lombok.Data;

import java.io.Serializable;

@Data
public class CheckinResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer pointsEarned;
    private Integer bonusEarned;
    private Integer continuousDays;
    private Boolean sevenDayBonus;
}
