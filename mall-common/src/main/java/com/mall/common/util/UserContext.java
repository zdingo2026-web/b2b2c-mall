package com.mall.common.util;

/**
 * User context holder for current user info.
 * Stores user info extracted from JWT token for the request lifecycle.
 * Named UserContext to avoid conflict with Spring Security's SecurityContextHolder.
 */
public final class UserContext {

    private UserContext() {}

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> USER_TYPE = new ThreadLocal<>();
    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setUserType(Integer userType) {
        USER_TYPE.set(userType);
    }

    public static Integer getUserType() {
        return USER_TYPE.get();
    }

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    public static void setUsername(String username) {
        USERNAME.set(username);
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static void clear() {
        USER_ID.remove();
        USER_TYPE.remove();
        TENANT_ID.remove();
        USERNAME.remove();
    }
}
