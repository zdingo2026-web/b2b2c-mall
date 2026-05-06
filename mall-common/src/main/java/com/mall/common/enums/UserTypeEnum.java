package com.mall.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * User type enum: platform admin, merchant admin, member(consumer).
 */
@Getter
@AllArgsConstructor
public enum UserTypeEnum {

    PLATFORM(1, "平台管理员"),
    MERCHANT(2, "商户管理员"),
    MEMBER(3, "消费者");

    private final int code;
    private final String desc;

    public static UserTypeEnum fromCode(int code) {
        for (UserTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown UserTypeEnum code: " + code);
    }
}
