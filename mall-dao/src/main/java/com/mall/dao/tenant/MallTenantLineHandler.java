package com.mall.dao.tenant;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.mall.common.constant.CommonConstant;
import com.mall.common.util.TenantContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;

/**
 * MyBatis-Plus tenant line handler.
 * Automatically injects tenant_id = ? condition into SQL statements.
 */
public class MallTenantLineHandler implements TenantLineHandler {

    @Override
    public Expression getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            // No tenant context: treat as platform (tenant_id = 0)
            return new LongValue(CommonConstant.PLATFORM_TENANT_ID);
        }
        return new LongValue(tenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }

    @Override
    public boolean ignoreTable(String tableName) {
        Long tenantId = TenantContext.getTenantId();
        // No tenant context (unauthenticated request) or platform admin — skip tenant filtering
        if (tenantId == null || tenantId == CommonConstant.PLATFORM_TENANT_ID) {
            return true;
        }
        // System tables without tenant_id
        return SYSTEM_TABLES.contains(tableName);
    }

    /**
     * Tables that do NOT have a tenant_id column for tenant isolation.
     * Platform-level tables and cross-tenant tables should be listed here.
     *
     * Note: tenant_apply has a tenant_id column that stores the generated tenant ID
     * after approval (not an isolation column). Do NOT remove it from this list.
     */
    private static final java.util.Set<String> SYSTEM_TABLES = new java.util.HashSet<>(java.util.Arrays.asList(
            "sys_admin",
            "sys_role",
            "sys_permission",
            "sys_role_permission",
            "sys_config",
            "region",
            "tenant",
            "tenant_category",
            "tenant_apply",
            "member",
            "member_address",
            "member_auth",
            "member_bank_card",
            "member_collect",
            "member_footprint",
            "member_coupon",
            "member_message",
            "content_notice",
            "order_log",
            "order_address",
            "product_category",
            "product_brand",
            "sys_file",
            "sms_code",
            // === 二期新增 ===
            "points_rule",
            "points_consume_rule",
            "points_account",
            "points_detail",
            "points_product_category",
            "distribution_config",
            "deco_template",
            "member_level",
            "member_checkin_record",
            "member_realname_auth",
            "newcomer_pack",
            "first_order_config",
            "tenant_level",
            "category_commission",
            "tenant_commission_settlement",
            "tenant_freeze_record",
            "tenant_settle_config",
            "member_paypwd_reset"
    ));
}
