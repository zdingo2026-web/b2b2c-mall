package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 物流信息VO
 */
@Data
public class OrderLogisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String deliveryNo;

    private String deliveryCompany;

    /** 物流状态: 0-无 1-已发货 2-运输中 3-已签收 */
    private Integer deliveryStatus;

    private String deliveryStatusText;

    /** 物流轨迹(简化) */
    private List<LogisticsTrace> traces;

    @Data
    public static class LogisticsTrace implements Serializable {

        private static final long serialVersionUID = 1L;

        private String time;

        private String description;
    }
}
