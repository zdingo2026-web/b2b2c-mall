package com.mall.common.constant;

import java.lang.annotation.*;

/**
 * Annotation to skip tenant filtering for specific mapper methods or service methods.
 * When a method is annotated with @TenantIgnore, the MyBatis-Plus tenant interceptor
 * will NOT inject tenant_id conditions for that query.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantIgnore {
}
