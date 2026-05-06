package com.mall.common.util;

/**
 * ThreadLocal-based tenant context holder.
 * Stores the current tenant ID for the request lifecycle.
 */
public final class TenantContext {

    private TenantContext() {}

    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    /**
     * Set current tenant ID.
     */
    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    /**
     * Get current tenant ID.
     */
    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * Clear tenant context (must be called in filter/interceptor finally block).
     */
    public static void clear() {
        TENANT_ID.remove();
    }

    /**
     * Check if tenant context is set.
     */
    public static boolean hasTenant() {
        return TENANT_ID.get() != null;
    }
}
