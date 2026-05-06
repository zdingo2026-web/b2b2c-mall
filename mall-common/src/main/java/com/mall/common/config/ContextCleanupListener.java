package com.mall.common.config;

import com.mall.common.util.TenantContext;
import com.mall.common.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

/**
 * Servlet request listener that clears UserContext and TenantContext
 * after each HTTP request completes. Prevents ThreadLocal leaks when
 * servlet threads are reused by the container thread pool.
 */
@Slf4j
@Component
public class ContextCleanupListener implements ServletRequestListener {

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        UserContext.clear();
        TenantContext.clear();
    }
}
