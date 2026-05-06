package com.mall.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Payment status enum.
 */
@Getter
@AllArgsConstructor
public enum PaymentStatusEnum {

    PENDING(0, "待支付"),
    PAID(1, "已支付"),
    FAILED(2, "支付失败"),
    REFUNDING(3, "退款中"),
    REFUNDED(4, "已退款"),
    CLOSED(5, "已关闭");

    private final int code;
    private final String desc;

    public static PaymentStatusEnum fromCode(int code) {
        for (PaymentStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown PaymentStatusEnum code: " + code);
    }
}
