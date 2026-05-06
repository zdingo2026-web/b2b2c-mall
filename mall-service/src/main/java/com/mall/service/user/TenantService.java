package com.mall.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.enums.TenantStatusEnum;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.TenantAdminMapper;
import com.mall.dao.mapper.TenantApplyMapper;
import com.mall.dao.mapper.TenantMapper;
import com.mall.model.dto.TenantApplyDTO;
import com.mall.model.entity.Tenant;
import com.mall.model.entity.TenantAdmin;
import com.mall.model.entity.TenantApply;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Tenant (merchant) service.
 * Handles apply, audit, tenant list/detail, and tenant admin CRUD.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantMapper tenantMapper;
    private final TenantApplyMapper tenantApplyMapper;
    private final TenantAdminMapper tenantAdminMapper;

    /**
     * Submit tenant apply.
     */
    @Transactional(rollbackFor = Exception.class)
    public Long apply(TenantApplyDTO dto) {
        // Check if business license already applied
        Long count = tenantApplyMapper.selectCount(
                new LambdaQueryWrapper<TenantApply>()
                        .eq(TenantApply::getBusinessLicense, dto.getBusinessLicense())
                        .ne(TenantApply::getApplyStatus, 2));
        if (count > 0) {
            throw new BusinessException("该营业执照已提交过申请");
        }

        TenantApply apply = new TenantApply();
        apply.setTenantName(dto.getTenantName());
        apply.setContactName(dto.getContactName());
        apply.setContactPhone(dto.getContactPhone());
        apply.setContactEmail(dto.getContactEmail());
        apply.setBusinessLicense(dto.getBusinessLicense());
        apply.setLicenseImage(dto.getLicenseImage());
        apply.setAddress(dto.getAddress());
        apply.setApplyStatus(0);
        tenantApplyMapper.insert(apply);

        return apply.getId();
    }

    /**
     * Audit tenant apply.
     */
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long applyId, boolean pass, String reason, Long auditUserId) {
        TenantApply apply = tenantApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (apply.getApplyStatus() != 0) {
            throw new BusinessException("该申请已审核");
        }

        apply.setAuditUserId(auditUserId);
        apply.setAuditTime(LocalDateTime.now());
        apply.setAuditRemark(reason);

        if (pass) {
            apply.setApplyStatus(1);

            // Create tenant
            Tenant tenant = new Tenant();
            tenant.setTenantName(apply.getTenantName());
            tenant.setContactName(apply.getContactName());
            tenant.setContactPhone(apply.getContactPhone());
            tenant.setContactEmail(apply.getContactEmail());
            tenant.setBusinessLicense(apply.getBusinessLicense());
            tenant.setLicenseImage(apply.getLicenseImage());
            tenant.setAddress(apply.getAddress());
            tenant.setStatus(TenantStatusEnum.APPROVED.getCode());
            tenant.setCommissionRate(new BigDecimal("10"));
            tenantMapper.insert(tenant);

            // Create default admin for tenant
            TenantAdmin admin = new TenantAdmin();
            admin.setTenantId(tenant.getId());
            admin.setUsername(apply.getContactPhone());
            admin.setPassword(com.mall.common.util.PasswordUtil.encode("123456"));
            admin.setRealName(apply.getContactName());
            admin.setPhone(apply.getContactPhone());
            admin.setRoleType(1);
            admin.setStatus(1);
            tenantAdminMapper.insert(admin);

            apply.setTenantId(tenant.getId());
        } else {
            apply.setApplyStatus(2);
        }

        tenantApplyMapper.updateById(apply);
    }

    /**
     * Get tenant list (paginated).
     */
    public PageResult<Tenant> getTenantList(Integer status, String keyword, int page, int limit) {
        Page<Tenant> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Tenant::getStatus, status);
        }
        // Add keyword search
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(Tenant::getTenantName, keyword)
                .or()
                .like(Tenant::getContactName, keyword)
                .or()
                .like(Tenant::getContactPhone, keyword)
            );
        }
        wrapper.orderByDesc(Tenant::getCreateTime);

        Page<Tenant> result = tenantMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, limit);
    }

    /**
     * Get tenant detail.
     */
    public Tenant getTenantDetail(Long tenantId) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BusinessException(ResultCode.TENANT_NOT_FOUND);
        }
        return tenant;
    }

    /**
     * Audit tenant directly (approve or reject).
     * Used by PUT /platform/tenant/{id}/audit.
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditTenant(Long tenantId, boolean pass, String reason, Long auditUserId) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BusinessException(ResultCode.TENANT_NOT_FOUND);
        }
        if (pass) {
            tenant.setStatus(TenantStatusEnum.APPROVED.getCode());
        } else {
            tenant.setStatus(TenantStatusEnum.REJECTED.getCode());
        }
        tenant.setAuditRemark(reason);
        tenantMapper.updateById(tenant);
    }

    /**
     * Enable tenant.
     */
    @Transactional(rollbackFor = Exception.class)
    public void enableTenant(Long tenantId) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BusinessException(ResultCode.TENANT_NOT_FOUND);
        }
        tenant.setStatus(TenantStatusEnum.APPROVED.getCode());
        tenantMapper.updateById(tenant);
    }

    /**
     * Disable tenant.
     */
    @Transactional(rollbackFor = Exception.class)
    public void disableTenant(Long tenantId) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BusinessException(ResultCode.TENANT_NOT_FOUND);
        }
        tenant.setStatus(TenantStatusEnum.DISABLED.getCode());
        tenantMapper.updateById(tenant);
    }

    // ==================== Tenant Admin CRUD ====================

    /**
     * Get tenant admin list.
     */
    public List<TenantAdmin> getTenantAdminList(Long tenantId) {
        return tenantAdminMapper.selectList(
                new LambdaQueryWrapper<TenantAdmin>()
                        .eq(TenantAdmin::getTenantId, tenantId)
                        .orderByAsc(TenantAdmin::getRoleType));
    }

    /**
     * Create tenant admin.
     */
    @Transactional(rollbackFor = Exception.class)
    public TenantAdmin createTenantAdmin(TenantAdmin admin) {
        // Check username uniqueness within tenant
        Long count = tenantAdminMapper.selectCount(
                new LambdaQueryWrapper<TenantAdmin>()
                        .eq(TenantAdmin::getTenantId, admin.getTenantId())
                        .eq(TenantAdmin::getUsername, admin.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_USERNAME_EXISTS);
        }
        admin.setPassword(com.mall.common.util.PasswordUtil.encode(admin.getPassword()));
        admin.setRoleType(2);
        admin.setStatus(1);
        tenantAdminMapper.insert(admin);
        return admin;
    }

    /**
     * Update tenant admin status.
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTenantAdminStatus(Long adminId, Integer status) {
        TenantAdmin admin = tenantAdminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        admin.setStatus(status);
        tenantAdminMapper.updateById(admin);
    }

    /**
     * Update tenant score and verification status.
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTenantScore(Long tenantId, BigDecimal scoreProduct, BigDecimal scoreService,
                                 BigDecimal scoreLogistics, Integer brandVerified) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BusinessException(ResultCode.TENANT_NOT_FOUND);
        }

        tenant.setScoreProduct(scoreProduct);
        tenant.setScoreService(scoreService);
        tenant.setScoreLogistics(scoreLogistics);
        tenant.setBrandVerified(brandVerified);

        tenantMapper.updateById(tenant);
    }
}
