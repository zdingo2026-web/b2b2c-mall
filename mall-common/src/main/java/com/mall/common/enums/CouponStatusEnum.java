package com.mall.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CouponStatusEnum {

    UNUSED(0, "未使用"),
    USED(1, "已使用"),
    EXPIRED(2, "已过期");

    private final int code;
    private final String desc;

    public static CouponStatusEnum fromCode(int code) {
        for (CouponStatusEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown CouponStatusEnum code: " + code);
    }
}
