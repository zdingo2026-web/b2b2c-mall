package com.mall.admin.controller.platform;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.dto.SpuCreateDTO;
import com.mall.model.dto.SpuQueryDTO;
import com.mall.model.entity.ProductBrand;
import com.mall.model.entity.ProductCategory;
import com.mall.model.vo.CategoryTreeVO;
import com.mall.model.vo.SpuDetailVO;
import com.mall.model.vo.SpuVO;
import com.mall.dao.mapper.ProductBrandMapper;
import com.mall.service.product.ProductCategoryService;
import com.mall.service.product.ProductSpuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Platform product management controller (for platform self-operated products).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/platform/product")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM')")
public class PlatformProductController {

    private final ProductSpuService productSpuService;
    private final ProductCategoryService productCategoryService;
    private final ProductBrandMapper productBrandMapper;

    // ==================== SPU ====================

    @PostMapping("/spu")
    public R<Long> createSpu(@Validated @RequestBody SpuCreateDTO dto) {
        // Platform self-operated: tenantId = 0
        Long tenantId = 0L;
        return R.ok(productSpuService.createSpu(dto, tenantId));
    }

    @PutMapping("/spu/{id}")
    public R<Void> updateSpu(@PathVariable Long id, @RequestBody SpuCreateDTO dto) {
        productSpuService.updateSpu(id, dto, 0L);
        return R.ok();
    }

    @GetMapping("/spu/list")
    public R<PageResult<SpuVO>> spuList(SpuQueryDTO dto) {
        return R.ok(productSpuService.getSpuList(dto, 0L));
    }

    @GetMapping("/spu/{id}")
    public R<SpuDetailVO> spuDetail(@PathVariable Long id) {
        return R.ok(productSpuService.getSpuDetail(id));
    }

    @PutMapping("/spu/{id}/publish")
    public R<Void> publishSpu(@PathVariable Long id) {
        productSpuService.publishSpu(id, 0L);
        return R.ok();
    }

    @PutMapping("/spu/{id}/unpublish")
    public R<Void> unpublishSpu(@PathVariable Long id) {
        productSpuService.unpublishSpu(id, 0L);
        return R.ok();
    }

    @PutMapping("/spu/{id}/audit")
    public R<Void> auditSpu(@PathVariable Long id,
                             @RequestParam Boolean pass,
                             @RequestParam(required = false) String reason) {
        productSpuService.auditSpu(id, pass, reason);
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
        dto.setTenantId(0L);
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

    // ==================== Brand ====================

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

    @PostMapping("/brand")
    public R<ProductBrand> createBrand(@RequestBody ProductBrand dto) {
        dto.setTenantId(0L);
        dto.setStatus(1);
        productBrandMapper.insert(dto);
        return R.ok(dto);
    }

    @PutMapping("/brand/{id}")
    public R<ProductBrand> updateBrand(@PathVariable Long id, @RequestBody ProductBrand dto) {
        ProductBrand brand = productBrandMapper.selectOne(
                new LambdaQueryWrapper<ProductBrand>()
                        .eq(ProductBrand::getId, id)
                        .eq(ProductBrand::getTenantId, 0L));
        if (brand == null) {
            return R.fail("品牌不存在或无权编辑");
        }
        if (dto.getBrandName() != null) {
            brand.setBrandName(dto.getBrandName());
        }
        if (dto.getLogo() != null) {
            brand.setLogo(dto.getLogo());
        }
        if (dto.getDescription() != null) {
            brand.setDescription(dto.getDescription());
        }
        if (dto.getSortOrder() != null) {
            brand.setSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            brand.setStatus(dto.getStatus());
        }
        productBrandMapper.updateById(brand);
        return R.ok(brand);
    }

    @DeleteMapping("/brand/{id}")
    public R<Void> deleteBrand(@PathVariable Long id) {
        int deleted = productBrandMapper.delete(
                new LambdaQueryWrapper<ProductBrand>()
                        .eq(ProductBrand::getId, id)
                        .eq(ProductBrand::getTenantId, 0L));
        if (deleted == 0) {
            return R.fail("品牌不存在或无权删除");
        }
        return R.ok();
    }
}
