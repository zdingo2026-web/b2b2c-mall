package com.mall.dao.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.mall.dao.tenant.MallTenantLineHandler;
import com.mall.dao.tenant.TenantSqlInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus configuration.
 * Configures: pagination, tenant interceptor, optimistic lock, logical delete, auto-fill.
 */
@Configuration
@MapperScan("com.mall.dao.mapper")
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus interceptor chain.
     * Order matters: tenant -> optimistic lock -> pagination
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. Tenant interceptor (must be first to inject tenant_id before pagination count)
        TenantSqlInterceptor tenantInterceptor = new TenantSqlInterceptor(new MallTenantLineHandler());
        interceptor.addInnerInterceptor(tenantInterceptor);

        // 2. Optimistic lock interceptor
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 3. Pagination interceptor
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(100L); // Max 100 items per page
        paginationInterceptor.setOverflow(false); // No overflow to first page
        interceptor.addInnerInterceptor(paginationInterceptor);

        return interceptor;
    }

    /**
     * Auto-fill handler for createTime and updateTime.
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
