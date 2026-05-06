package com.mall.common.config;

import com.mall.common.util.TenantContext;
import com.mall.common.util.UserContext;
import org.springframework.core.task.TaskDecorator;

/**
 * TaskDecorator that propagates UserContext and TenantContext
 * from the submitting thread to the worker thread in async contexts
 * (CompletableFuture, @Async, thread pool executors).
 *
 * Captures all context values at decoration time and sets them
 * before execution, then clears them in a finally block to prevent
 * ThreadLocal leaks on pooled threads.
 */
public class ContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // Capture context from the submitting thread
        Long userId = UserContext.getUserId();
        Integer userType = UserContext.getUserType();
        Long userTenantId = UserContext.getTenantId();
        String username = UserContext.getUsername();
        Long tenantId = TenantContext.getTenantId();

        return () -> {
            try {
                // Set context in the worker thread
                if (userId != null) UserContext.setUserId(userId);
                if (userType != null) UserContext.setUserType(userType);
                if (userTenantId != null) UserContext.setTenantId(userTenantId);
                if (username != null) UserContext.setUsername(username);
                if (tenantId != null) TenantContext.setTenantId(tenantId);
                runnable.run();
            } finally {
                // Clear context to prevent leaks on reused threads
                UserContext.clear();
                TenantContext.clear();
            }
        };
    }
}
