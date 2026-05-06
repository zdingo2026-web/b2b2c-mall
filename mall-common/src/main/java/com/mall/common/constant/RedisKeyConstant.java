package com.mall.common.constant;

/**
 * Redis key constants.
 */
public final class RedisKeyConstant {

    private RedisKeyConstant() {}

    private static final String BASE = CommonConstant.REDIS_KEY_PREFIX;

    /** SMS verification code: sms:code:{phone} */
    public static final String SMS_CODE = BASE + "sms:code:";

    /** Token blacklist: token:blacklist:{token} */
    public static final String TOKEN_BLACKLIST = BASE + "token:blacklist:";

    /** Member info cache: member:info:{id} */
    public static final String MEMBER_INFO = BASE + "member:info:";

    /** Product stock lock: product:stock:{skuId} */
    public static final String PRODUCT_STOCK = BASE + "product:stock:";

    /** Cart cache: cart:{memberId} */
    public static final String CART = BASE + "cart:";

    /** SMS send interval: sms:interval:{phone} */
    public static final String SMS_INTERVAL = BASE + "sms:interval:";

    /** SMS daily count: sms:count:{phone}:{date} */
    public static final String SMS_DAILY_COUNT = BASE + "sms:count:";

    /** SMS verify attempts: sms:verify:{phone} */
    public static final String SMS_VERIFY_ATTEMPTS = BASE + "sms:verify:";

    /** Payment record lock: payment:lock:{paymentNo} */
    public static final String PAYMENT_LOCK = BASE + "payment:lock:";

    /** Payment order lock: payment:order:lock:{orderId} */
    public static final String PAYMENT_ORDER_LOCK = BASE + "payment:order:lock:";

    /** Withdraw lock: payment:withdraw:lock:{tenantId} */
    public static final String WITHDRAW_LOCK = BASE + "payment:withdraw:lock:";

    /** Rate limit: rate:{key} */
    public static final String RATE_LIMIT = BASE + "rate:";
}
