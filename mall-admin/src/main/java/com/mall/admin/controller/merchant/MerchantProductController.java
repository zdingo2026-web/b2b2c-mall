package com.mall.admin.controller.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.dao.mapper.ProductBrandMapper;
import com.mall.model.dto.SpuCreateDTO;
import com.mall.model.dto.SpuQueryDTO;
import com.mall.model.entity.ProductBrand;
import com.mall.model.entity.ProductCategory;
import com.mall.model.vo.CategoryTreeVO;
import com.mall.model.vo.SpuDetailVO;
import com.mall.model.vo.SpuVO;
import com.mall.service.product.ProductCategoryService;
import com.mall.service.product.ProductSpuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Merchant product management controller.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/merchant/product")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantProductController {

    private final ProductSpuService productSpuService;
    private final ProductCategoryService productCategoryService;
    private final ProductBrandMapper productBrandMapper;

    // ==================== SPU ====================

    @PostMapping("/spu")
    public R<Long> createSpu(@Validated @RequestBody SpuCreateDTO dto) {
        Long tenantId = UserContext.getTenantId();
        return R.ok(productSpuService.createSpu(dto, tenantId));
    }

    @PutMapping("/spu/{id}")
    public R<Void> updateSpu(@PathVariable Long id, @RequestBody SpuCreateDTO dto) {
        Long tenantId = UserContext.getTenantId();
        productSpuService.updateSpu(id, dto, tenantId);
        return R.ok();
    }

    @GetMapping("/spu/list")
    public R<PageResult<SpuVO>> spuList(SpuQueryDTO dto) {
        Long tenantId = UserContext.getTenantId();
        return R.ok(productSpuService.getSpuList(dto, tenantId));
    }

    @GetMapping("/spu/{id}")
    public R<SpuDetailVO> spuDetail(@PathVariable Long id) {
        return R.ok(productSpuService.getSpuDetail(id));
    }

    @PutMapping("/spu/{id}/publish")
    public R<Void> publishSpu(@PathVariable Long id) {
        Long tenantId = UserContext.getTenantId();
        productSpuService.publishSpu(id, tenantId);
        return R.ok();
    }

    @PutMapping("/spu/{id}/unpublish")
    public R<Void> unpublishSpu(@PathVariable Long id) {
        Long tenantId = UserContext.getTenantId();
        productSpuService.unpublishSpu(id, tenantId);
        return R.ok();
    }

    @DeleteMapping("/spu/{id}")
    public R<Void> deleteSpu(@PathVariable Long id) {
        Long tenantId = UserContext.getTenantId();
        productSpuService.updateSpu(id, new SpuCreateDTO(), tenantId);
        return R.ok();
    }

    // ==================== Category ====================

    @GetMapping("/category/tree")
    public R<List<CategoryTreeVO>> categoryTree() {
        return R.ok(productCategoryService.getCategoryTree(0L));
    }

    @GetMapping("/category/list")
    public R<List<ProductCategory>> categoryList() {
        return R.ok(productCategoryService.getCategoryList(0L));
    }

    @PostMapping("/category")
    public R<ProductCategory> createCategory(@RequestBody ProductCategory dto) {
        Long tenantId = UserContext.getTenantId();
        dto.setTenantId(tenantId);
        return R.ok(productCategoryService.createCategory(dto));
    }

    @PutMapping("/category/{id}")
    public R<ProductCategory> updateCategory(@PathVariable Long id, @RequestBody ProductCategory dto) {
        return R.ok(productCategoryService.updateCategory(id, dto));
    }

    @DeleteMapping("/category/{id}")
    public R<Void> deleteCategory(@PathVariable Long id) {
        productCategoryService.deleteCategory(id);
        return R.ok();
    }

    // ==================== Brand (read-only) ====================

    @GetMapping("/brand/list")
    public R<PageResult<ProductBrand>> brandList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Page<ProductBrand> pageParam = new Page<>(page, limit);
        Page<ProductBrand> result = productBrandMapper.selectPage(pageParam,
                new LambdaQueryWrapper<ProductBrand>()
                        .eq(ProductBrand::getTenantId, 0L)
                        .orderByAsc(ProductBrand::getSortOrder));
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, limit));
    }
}
