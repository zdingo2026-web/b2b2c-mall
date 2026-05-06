package com.mall.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Refund status enum.
 */
@Getter
@AllArgsConstructor
public enum RefundStatusEnum {

    REFUNDING(0, "退款中"),
    SUCCESS(1, "退款成功"),
    FAIL(2, "退款失败");

    private final int code;
    private final String desc;

    public static RefundStatusEnum fromCode(int code) {
        for (RefundStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown RefundStatusEnum code: " + code);
    }
}
