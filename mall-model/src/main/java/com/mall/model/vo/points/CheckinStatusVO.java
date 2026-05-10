package com.mall.model.vo.points;

import lombok.Data;

import java.io.Serializable;

@Data
public class CheckinStatusVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean todayChecked;
    private Integer continuousDays;
    private Integer nextBonusDays;
}
