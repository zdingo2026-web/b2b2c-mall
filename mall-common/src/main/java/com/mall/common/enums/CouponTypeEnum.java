package com.mall.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CouponTypeEnum {

    FULL_REDUCTION(1, "满减"),
    DISCOUNT(2, "满折"),
    GIFT(3, "满赠"),
    NO_THRESHOLD(4, "无门槛减");

    private final int code;
    private final String desc;

    public static CouponTypeEnum fromCode(int code) {
        for (CouponTypeEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown CouponTypeEnum code: " + code);
    }
}
