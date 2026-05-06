package com.mall.admin.controller.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.R;
import com.mall.common.response.ResultCode;
import com.mall.common.util.PasswordUtil;
import com.mall.common.util.UserContext;
import com.mall.dao.mapper.TenantAdminMapper;
import com.mall.model.dto.SubAdminCreateDTO;
import com.mall.model.dto.SubAdminUpdateDTO;
import com.mall.model.entity.TenantAdmin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Merchant sub-admin management controller.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/merchant/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantAdminController {

    private final TenantAdminMapper tenantAdminMapper;

    @GetMapping("/list")
    public R<List<TenantAdmin>> list() {
        Long tenantId = UserContext.getTenantId();
        List<TenantAdmin> admins = tenantAdminMapper.selectList(
                new LambdaQueryWrapper<TenantAdmin>()
                        .eq(TenantAdmin::getTenantId, tenantId)
                        .orderByAsc(TenantAdmin::getRoleType));
        return R.ok(admins);
    }

    @PostMapping
    public R<Long> create(@Validated @RequestBody SubAdminCreateDTO dto) {
        Long tenantId = UserContext.getTenantId();

        // Verify caller is main admin (roleType=1)
        Long currentAdminId = UserContext.getUserId();
        TenantAdmin currentAdmin = tenantAdminMapper.selectById(currentAdminId);
        if (currentAdmin == null || currentAdmin.getRoleType() != 1) {
            throw new BusinessException("仅主管理员可添加子管理员");
        }

        // Check username uniqueness within tenant
        Long count = tenantAdminMapper.selectCount(
                new LambdaQueryWrapper<TenantAdmin>()
                        .eq(TenantAdmin::getTenantId, tenantId)
                        .eq(TenantAdmin::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_USERNAME_EXISTS);
        }

        TenantAdmin admin = new TenantAdmin();
        admin.setTenantId(tenantId);
        admin.setUsername(dto.getUsername());
        admin.setPassword(PasswordUtil.encode(dto.getPassword()));
        admin.setRealName(dto.getRealName());
        admin.setPhone(dto.getPhone());
        admin.setEmail(dto.getEmail());
        admin.setRoleType(2);
        admin.setStatus(1);
        tenantAdminMapper.insert(admin);
        return R.ok(admin.getId());
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Validated @RequestBody SubAdminUpdateDTO dto) {
        Long tenantId = UserContext.getTenantId();
        TenantAdmin admin = tenantAdminMapper.selectById(id);
        if (admin == null || !admin.getTenantId().equals(tenantId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (admin.getRoleType() == 1) {
            throw new BusinessException("不能修改主管理员信息");
        }
        admin.setRealName(dto.getRealName());
        admin.setPhone(dto.getPhone());
        admin.setEmail(dto.getEmail());
        tenantAdminMapper.updateById(admin);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Long tenantId = UserContext.getTenantId();
        TenantAdmin admin = tenantAdminMapper.selectById(id);
        if (admin == null || !admin.getTenantId().equals(tenantId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (admin.getRoleType() == 1) {
            throw new BusinessException("不能禁用主管理员");
        }
        admin.setStatus(status);
        tenantAdminMapper.updateById(admin);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Long tenantId = UserContext.getTenantId();
        TenantAdmin admin = tenantAdminMapper.selectById(id);
        if (admin == null || !admin.getTenantId().equals(tenantId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (admin.getRoleType() == 1) {
            throw new BusinessException("不能删除主管理员");
        }
        Long currentAdminId = UserContext.getUserId();
        if (admin.getId().equals(currentAdminId)) {
            throw new BusinessException("不能删除自己");
        }
        tenantAdminMapper.deleteById(id);
        return R.ok();
    }
}
