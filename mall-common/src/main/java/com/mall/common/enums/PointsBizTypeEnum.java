package com.mall.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PointsBizTypeEnum {

    ORDER(1, "购物"),
    CHECKIN(2, "签到"),
    REVIEW(3, "评价"),
    SYSTEM(4, "系统发放"),
    EXCHANGE(5, "积分兑换"),
    EXPIRY(6, "积分过期");

    private final int code;
    private final String desc;

    public static PointsBizTypeEnum fromCode(int code) {
        for (PointsBizTypeEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown PointsBizTypeEnum code: " + code);
    }
}
