package com.mall.admin.controller.platform;

import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.dto.tenant.CategoryCommissionDTO;
import com.mall.model.dto.tenant.TenantFreezeDTO;
import com.mall.model.dto.tenant.TenantLevelDTO;
import com.mall.model.dto.tenant.TenantSettleConfigDTO;
import com.mall.model.vo.tenant.CategoryCommissionVO;
import com.mall.model.vo.tenant.TenantFreezeVO;
import com.mall.model.vo.tenant.TenantLevelVO;
import com.mall.model.vo.tenant.TenantSettleConfigVO;
import com.mall.model.vo.tenant.TenantSettlementDetailVO;
import com.mall.model.vo.tenant.TenantSettlementVO;
import com.mall.service.tenant.CategoryCommissionService;
import com.mall.service.tenant.TenantCommissionSettlementService;
import com.mall.service.tenant.TenantFreezeService;
import com.mall.service.tenant.TenantLevelService;
import com.mall.service.tenant.TenantSettleConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/platform/tenant-manage")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM')")
public class PlatformTenantManageController {

    private final TenantLevelService tenantLevelService;
    private final CategoryCommissionService categoryCommissionService;
    private final TenantCommissionSettlementService tenantCommissionSettlementService;
    private final TenantFreezeService tenantFreezeService;
    private final TenantSettleConfigService tenantSettleConfigService;

    @GetMapping("/level/list")
    public R<List<TenantLevelVO>> levelList() {
        return R.ok(tenantLevelService.list());
    }

    @PostMapping("/level/create")
    public R<Void> createLevel(@Validated @RequestBody TenantLevelDTO dto) {
        tenantLevelService.create(dto);
        return R.ok();
    }

    @PutMapping("/level/{id}")
    public R<Void> updateLevel(@PathVariable Long id, @Validated @RequestBody TenantLevelDTO dto) {
        tenantLevelService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/level/{id}")
    public R<Void> deleteLevel(@PathVariable Long id) {
        tenantLevelService.delete(id);
        return R.ok();
    }

    @GetMapping("/commission/list")
    public R<List<CategoryCommissionVO>> commissionList() {
        return R.ok(categoryCommissionService.list());
    }

    @PutMapping("/commission/batch")
    public R<Void> batchSaveCommission(@RequestBody CategoryCommissionDTO dto) {
        categoryCommissionService.batchSave(dto);
        return R.ok();
    }

    @GetMapping("/settlement/list")
    public R<PageResult<TenantSettlementVO>> settlementList(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(tenantCommissionSettlementService.list(tenantId, status, page, limit));
    }

    @GetMapping("/settlement/{id}")
    public R<TenantSettlementDetailVO> settlementDetail(@PathVariable Long id) {
        return R.ok(tenantCommissionSettlementService.detail(id));
    }

    @PutMapping("/settlement/{id}/settle")
    public R<Void> settle(@PathVariable Long id) {
        tenantCommissionSettlementService.settle(id);
        return R.ok();
    }

    @GetMapping("/freeze/list")
    public R<PageResult<TenantFreezeVO>> freezeList(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(tenantFreezeService.list(tenantId, page, limit));
    }

    @PostMapping("/freeze/{tenantId}")
    public R<Void> freeze(@PathVariable Long tenantId, @Validated @RequestBody TenantFreezeDTO dto) {
        tenantFreezeService.freeze(tenantId, dto, UserContext.getUserId());
        return R.ok();
    }

    @PutMapping("/freeze/{tenantId}/unfreeze")
    public R<Void> unfreeze(@PathVariable Long tenantId) {
        tenantFreezeService.unfreeze(tenantId, UserContext.getUserId());
        return R.ok();
    }

    @GetMapping("/settle-config")
    public R<TenantSettleConfigVO> getSettleConfig() {
        return R.ok(tenantSettleConfigService.getConfig());
    }

    @PutMapping("/settle-config")
    public R<Void> saveSettleConfig(@Validated @RequestBody TenantSettleConfigDTO dto) {
        tenantSettleConfigService.saveConfig(dto);
        return R.ok();
    }
}
