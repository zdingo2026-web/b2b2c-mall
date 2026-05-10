package com.mall.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommissionStatusEnum {

    FROZEN(0, "冻结"),
    AVAILABLE(1, "可提现"),
    WITHDRAWING(2, "提现中"),
    WITHDRAWN(3, "已提现"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String desc;

    public static CommissionStatusEnum fromCode(int code) {
        for (CommissionStatusEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown CommissionStatusEnum code: " + code);
    }
}
