package com.mall.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PromotionTypeEnum {

    COUPON(1, "优惠券"),
    SECKILL(2, "秒杀"),
    GROUP_BUY(3, "拼团"),
    GROUPON(4, "团购"),
    DISCOUNT(5, "限时折扣"),
    FIRST_ORDER(6, "首单优惠"),
    NEWCOMER_PACK(7, "新人礼包");

    private final int code;
    private final String desc;

    public static PromotionTypeEnum fromCode(int code) {
        for (PromotionTypeEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown PromotionTypeEnum code: " + code);
    }
}
