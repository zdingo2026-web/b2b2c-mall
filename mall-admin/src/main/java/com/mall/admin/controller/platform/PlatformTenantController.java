package com.mall.admin.controller.platform;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.dto.TenantApplyDTO;
import com.mall.model.dto.TenantCategoryDTO;
import com.mall.model.entity.Tenant;
import com.mall.model.entity.TenantApply;
import com.mall.model.entity.TenantCategory;
import com.mall.service.user.TenantService;
import com.mall.service.user.TenantCategoryService;
import com.mall.dao.mapper.TenantApplyMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Platform tenant management controller.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/platform/tenant")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM')")
public class PlatformTenantController {

    private final TenantService tenantService;
    private final TenantCategoryService tenantCategoryService;
    private final TenantApplyMapper tenantApplyMapper;

    @PostMapping("/apply")
    public R<Long> apply(@Validated @RequestBody TenantApplyDTO dto) {
        return R.ok(tenantService.apply(dto));
    }

    /**
     * Get tenant apply list (for audit).
     */
    @GetMapping("/apply/list")
    public R<PageResult<TenantApply>> applyList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Page<TenantApply> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<TenantApply> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(TenantApply::getApplyStatus, status);
        }
        wrapper.orderByDesc(TenantApply::getCreateTime);
        Page<TenantApply> result = tenantApplyMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, limit));
    }

    /**
     * Get tenant apply detail.
     */
    @GetMapping("/apply/{id}")
    public R<TenantApply> applyDetail(@PathVariable Long id) {
        TenantApply apply = tenantApplyMapper.selectById(id);
        if (apply == null) {
            return R.fail("申请记录不存在");
        }
        return R.ok(apply);
    }

    /**
     * Audit tenant apply.
     */
    @PutMapping("/apply/{id}/audit")
    public R<Void> auditApply(@PathVariable Long id,
                               @RequestParam Boolean pass,
                               @RequestParam(required = false) String reason) {
        Long auditUserId = UserContext.getUserId();
        tenantService.audit(id, pass, reason, auditUserId);
        return R.ok();
    }

    @GetMapping("/list")
    public R<PageResult<Tenant>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(tenantService.getTenantList(status, keyword, page, limit));
    }

    @GetMapping("/{id}")
    public R<Tenant> detail(@PathVariable Long id) {
        return R.ok(tenantService.getTenantDetail(id));
    }

    @PutMapping("/{id}/audit")
    public R<Void> audit(@PathVariable Long id,
                          @RequestParam Boolean pass,
                          @RequestParam(required = false) String reason) {
        Long auditUserId = UserContext.getUserId();
        tenantService.auditTenant(id, pass, reason, auditUserId);
        return R.ok();
    }

    @PutMapping("/{id}/enable")
    public R<Void> enable(@PathVariable Long id) {
        tenantService.enableTenant(id);
        return R.ok();
    }

    @PutMapping("/{id}/disable")
    public R<Void> disable(@PathVariable Long id) {
        tenantService.disableTenant(id);
        return R.ok();
    }

    /**
     * Update tenant score and brand verification status.
     */
    @PutMapping("/{id}/score")
    public R<Void> updateScore(
            @PathVariable Long id,
            @Validated @RequestBody TenantScoreDTO scoreDTO) {
        tenantService.updateTenantScore(id, scoreDTO.getScoreProduct(), scoreDTO.getScoreService(),
                scoreDTO.getScoreLogistics(), scoreDTO.getBrandVerified());
        return R.ok();
    }

    // ==================== Tenant Category CRUD ====================

    /**
     * List all tenant categories.
     */
    @GetMapping("/category/list")
    public R<java.util.List<TenantCategory>> categoryList() {
        return R.ok(tenantCategoryService.list());
    }

    /**
     * Create a tenant category.
     */
    @PostMapping("/category/create")
    public R<Long> categoryCreate(@Validated @RequestBody TenantCategoryDTO dto) {
        return R.ok(tenantCategoryService.create(dto));
    }

    /**
     * Update a tenant category.
     */
    @PutMapping("/category/update")
    public R<Void> categoryUpdate(@RequestParam Long id,
                                   @Validated @RequestBody TenantCategoryDTO dto) {
        tenantCategoryService.update(id, dto);
        return R.ok();
    }

    /**
     * Delete a tenant category.
     */
    @DeleteMapping("/category/{id}")
    public R<Void> categoryDelete(@PathVariable Long id) {
        tenantCategoryService.delete(id);
        return R.ok();
    }

    /**
     * Tenant score update DTO.
     */
    @Data
    public static class TenantScoreDTO {
        @NotNull(message = "商品评分不能为空")
        private BigDecimal scoreProduct;

        @NotNull(message = "服务评分不能为空")
        private BigDecimal scoreService;

        @NotNull(message = "物流评分不能为空")
        private BigDecimal scoreLogistics;

        @NotNull(message = "品牌认证状态不能为空")
        private Integer brandVerified;
    }
}
