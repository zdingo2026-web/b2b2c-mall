package com.mall.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Payment type enum.
 */
@Getter
@AllArgsConstructor
public enum PayTypeEnum {

    WECHAT(1, "微信支付"),
    ALIPAY(2, "支付宝"),
    BALANCE(3, "余额支付");

    private final int code;
    private final String desc;

    public static PayTypeEnum fromCode(int code) {
        for (PayTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown PayTypeEnum code: " + code);
    }
}
