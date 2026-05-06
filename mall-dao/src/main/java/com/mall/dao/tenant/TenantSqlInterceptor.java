package com.mall.dao.tenant;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.mall.common.constant.TenantIgnore;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom tenant interceptor that respects @TenantIgnore annotation.
 * Extends TenantLineInnerInterceptor to check if the mapper method
 * is annotated with @TenantIgnore and skips tenant filtering if so.
 */
public class TenantSqlInterceptor extends TenantLineInnerInterceptor {

    private final Map<String, Boolean> tenantIgnoreCache = new ConcurrentHashMap<>();

    public TenantSqlInterceptor(TenantLineHandler tenantLineHandler) {
        super(tenantLineHandler);
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms,
                            Object parameter, org.apache.ibatis.session.RowBounds rowBounds,
                            org.apache.ibatis.session.ResultHandler resultHandler,
                            org.apache.ibatis.mapping.BoundSql boundSql) throws SQLException {
        if (isTenantIgnore(ms)) {
            return;
        }
        super.beforeQuery(executor, ms, parameter, rowBounds, resultHandler, boundSql);
    }

    @Override
    public boolean willDoUpdate(Executor executor, MappedStatement ms, Object updateObject) throws SQLException {
        if (isTenantIgnore(ms)) {
            return true;
        }
        return super.willDoUpdate(executor, ms, updateObject);
    }

    private boolean isTenantIgnore(MappedStatement ms) {
        return tenantIgnoreCache.computeIfAbsent(ms.getId(), id -> {
            try {
                int lastDot = id.lastIndexOf(".");
                String className = id.substring(0, lastDot);
                String methodName = id.substring(lastDot + 1);
                Class<?> mapperClass = Class.forName(className);
                // Check method-level annotation
                for (Method method : mapperClass.getMethods()) {
                    if (method.getName().equals(methodName) && method.isAnnotationPresent(TenantIgnore.class)) {
                        return true;
                    }
                }
                // Check class-level annotation
                return mapperClass.isAnnotationPresent(TenantIgnore.class);
            } catch (ClassNotFoundException e) {
                return false;
            }
        });
    }
}
