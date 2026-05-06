package com.mall.common.constant;

/**
 * Common constants.
 */
public final class CommonConstant {

    private CommonConstant() {}

    /** Platform tenant id */
    public static final Long PLATFORM_TENANT_ID = 0L;

    /** Default page size */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** Max page size */
    public static final int MAX_PAGE_SIZE = 100;

    /** Token header name */
    public static final String TOKEN_HEADER = "Authorization";

    /** Token prefix */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** Redis key prefix */
    public static final String REDIS_KEY_PREFIX = "mall:";

    /** Super admin role code */
    public static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
}
