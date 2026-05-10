package com.mall.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GroupBuyStatusEnum {

    PENDING(0, "拼团中"),
    SUCCESS(1, "成团成功"),
    FAILED(2, "拼团失败");

    private final int code;
    private final String desc;

    public static GroupBuyStatusEnum fromCode(int code) {
        for (GroupBuyStatusEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown GroupBuyStatusEnum code: " + code);
    }
}
