package com.mall.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Tenant status enum.
 */
@Getter
@AllArgsConstructor
public enum TenantStatusEnum {

    PENDING_REVIEW(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝"),
    DISABLED(3, "已禁用");

    private final int code;
    private final String desc;

    public static TenantStatusEnum fromCode(int code) {
        for (TenantStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown TenantStatusEnum code: " + code);
    }
}
