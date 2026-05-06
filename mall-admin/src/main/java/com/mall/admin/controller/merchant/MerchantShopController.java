package com.mall.admin.controller.merchant;

import com.mall.common.exception.BusinessException;
import com.mall.common.response.R;
import com.mall.common.response.ResultCode;
import com.mall.common.util.UserContext;
import com.mall.dao.mapper.TenantMapper;
import com.mall.model.dto.ShopUpdateDTO;
import com.mall.model.entity.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Merchant shop settings controller.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/merchant/shop")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantShopController {

    private final TenantMapper tenantMapper;

    @GetMapping
    public R<Tenant> getShopInfo() {
        Long tenantId = UserContext.getTenantId();
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return R.ok(tenant);
    }

    @PutMapping
    public R<Void> updateShopInfo(@Validated @RequestBody ShopUpdateDTO dto) {
        Long tenantId = UserContext.getTenantId();
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        tenant.setTenantName(dto.getTenantName());
        tenant.setLogo(dto.getLogo());
        tenant.setDescription(dto.getDescription());
        tenant.setContactName(dto.getContactName());
        tenant.setContactPhone(dto.getContactPhone());
        tenant.setContactEmail(dto.getContactEmail());
        tenant.setAddress(dto.getAddress());
        tenantMapper.updateById(tenant);
        return R.ok();
    }
}
