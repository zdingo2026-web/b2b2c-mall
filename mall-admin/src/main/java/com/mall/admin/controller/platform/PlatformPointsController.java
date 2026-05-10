package com.mall.admin.controller.platform;

import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.model.dto.points.PointsConsumeRuleDTO;
import com.mall.model.dto.points.PointsProductCreateDTO;
import com.mall.model.dto.points.PointsRuleDTO;
import com.mall.model.entity.points.PointsProductCategory;
import com.mall.model.vo.points.PointsConsumeRuleVO;
import com.mall.model.vo.points.PointsProductVO;
import com.mall.model.vo.points.PointsRuleVO;
import com.mall.service.points.PointsConsumeRuleService;
import com.mall.service.points.PointsProductCategoryService;
import com.mall.service.points.PointsProductService;
import com.mall.service.points.PointsRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/platform/points")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM')")
public class PlatformPointsController {

    private final PointsRuleService pointsRuleService;
    private final PointsConsumeRuleService pointsConsumeRuleService;
    private final PointsProductCategoryService pointsProductCategoryService;
    private final PointsProductService pointsProductService;

    @GetMapping("/rule/list")
    public R<List<PointsRuleVO>> ruleList() {
        return R.ok(pointsRuleService.list());
    }

    @PostMapping("/rule/save")
    public R<Void> saveRule(@Validated @RequestBody PointsRuleDTO dto) {
        pointsRuleService.save(dto);
        return R.ok();
    }

    @PutMapping("/rule/{id}/toggle")
    public R<Void> toggleRule(@PathVariable Long id) {
        pointsRuleService.toggleEnabled(id);
        return R.ok();
    }

    @GetMapping("/consume-rule")
    public R<PointsConsumeRuleVO> getConsumeRule() {
        return R.ok(pointsConsumeRuleService.getConfig());
    }

    @PutMapping("/consume-rule")
    public R<Void> saveConsumeRule(@Validated @RequestBody PointsConsumeRuleDTO dto) {
        pointsConsumeRuleService.saveConfig(dto);
        return R.ok();
    }

    @GetMapping("/category/list")
    public R<List<PointsProductCategory>> categoryList() {
        return R.ok(pointsProductCategoryService.list());
    }

    @PostMapping("/category/save")
    public R<Void> saveCategory(@RequestBody PointsProductCategory category) {
        pointsProductCategoryService.save(category);
        return R.ok();
    }

    @DeleteMapping("/category/{id}")
    public R<Void> deleteCategory(@PathVariable Long id) {
        pointsProductCategoryService.delete(id);
        return R.ok();
    }

    @GetMapping("/product/list")
    public R<PageResult<PointsProductVO>> productList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(pointsProductService.list(categoryId, status, page, limit));
    }

    @PostMapping("/product/create")
    public R<Void> createProduct(@Validated @RequestBody PointsProductCreateDTO dto) {
        pointsProductService.create(0L, dto);
        return R.ok();
    }

    @PutMapping("/product/{id}")
    public R<Void> updateProduct(@PathVariable Long id, @Validated @RequestBody PointsProductCreateDTO dto) {
        pointsProductService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/product/{id}")
    public R<Void> deleteProduct(@PathVariable Long id) {
        pointsProductService.delete(id);
        return R.ok();
    }

    @PutMapping("/product/{id}/status")
    public R<Void> updateProductStatus(@PathVariable Long id, @RequestParam Integer status) {
        pointsProductService.updateStatus(id, status);
        return R.ok();
    }
}
