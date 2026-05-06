package com.mall.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Product status enum.
 */
@Getter
@AllArgsConstructor
public enum ProductStatusEnum {

    DRAFT(0, "草稿"),
    ON_SHELF(1, "上架"),
    OFF_SHELF(2, "下架"),
    VIOLATION(3, "违规下架");

    private final int code;
    private final String desc;

    public static ProductStatusEnum fromCode(int code) {
        for (ProductStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ProductStatusEnum code: " + code);
    }
}
