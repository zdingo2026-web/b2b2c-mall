package com.mall.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderTypeEnum {

    NORMAL(1, "普通订单"),
    SECKILL(2, "秒杀订单"),
    GROUP_BUY(3, "拼团订单"),
    GROUPON(4, "团购订单"),
    POINTS(5, "积分兑换");

    private final int code;
    private final String desc;

    public static OrderTypeEnum fromCode(int code) {
        for (OrderTypeEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown OrderTypeEnum code: " + code);
    }
}
