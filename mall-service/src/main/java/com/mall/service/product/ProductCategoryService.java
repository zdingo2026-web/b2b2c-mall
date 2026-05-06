package com.mall.service.product;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.ProductCategoryMapper;
import com.mall.model.entity.ProductCategory;
import com.mall.model.vo.CategoryTreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Product category service.
 * Supports category tree retrieval with tenant isolation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryMapper productCategoryMapper;

    /**
     * Get category tree.
     * tenantId=0 for platform categories, otherwise merchant-specific.
     */
    public List<CategoryTreeVO> getCategoryTree(Long tenantId) {
        List<ProductCategory> allCategories = productCategoryMapper.selectList(
                new LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getTenantId, tenantId)
                        .eq(ProductCategory::getStatus, 1)
                        .orderByAsc(ProductCategory::getSortOrder));

        List<CategoryTreeVO> voList = allCategories.stream()
                .map(this::toCategoryTreeVO)
                .collect(Collectors.toList());

        return buildTree(voList);
    }

    /**
     * Get all categories (flat list) for a tenant.
     */
    public List<ProductCategory> getCategoryList(Long tenantId) {
        return productCategoryMapper.selectList(
                new LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getTenantId, tenantId)
                        .orderByAsc(ProductCategory::getSortOrder));
    }

    /**
     * Get category by ID (platform only).
     */
    public ProductCategory getCategory(Long categoryId) {
        ProductCategory category = productCategoryMapper.selectOne(
                new LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getId, categoryId)
                        .eq(ProductCategory::getTenantId, 0L));
        if (category == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return category;
    }

    /**
     * Create category.
     */
    @Transactional(rollbackFor = Exception.class)
    public ProductCategory createCategory(ProductCategory category) {
        // Determine level based on parent
        if (category.getParentId() == null || category.getParentId() == 0) {
            category.setLevel(1);
            category.setParentId(0L);
        } else {
            ProductCategory parent = productCategoryMapper.selectOne(
                    new LambdaQueryWrapper<ProductCategory>()
                            .eq(ProductCategory::getId, category.getParentId())
                            .eq(ProductCategory::getTenantId, 0L));
            if (parent == null) {
                throw new BusinessException("父分类不存在");
            }
            category.setLevel(parent.getLevel() + 1);
        }
        category.setStatus(1);
        productCategoryMapper.insert(category);
        return category;
    }

    /**
     * Update category (platform only).
     */
    @Transactional(rollbackFor = Exception.class)
    public ProductCategory updateCategory(Long categoryId, ProductCategory dto) {
        ProductCategory category = productCategoryMapper.selectOne(
                new LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getId, categoryId)
                        .eq(ProductCategory::getTenantId, 0L));
        if (category == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (dto.getCategoryName() != null) {
            category.setCategoryName(dto.getCategoryName());
        }
        if (dto.getIcon() != null) {
            category.setIcon(dto.getIcon());
        }
        if (dto.getImage() != null) {
            category.setImage(dto.getImage());
        }
        if (dto.getSortOrder() != null) {
            category.setSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            category.setStatus(dto.getStatus());
        }
        productCategoryMapper.updateById(category);
        return category;
    }

    /**
     * Delete category (and all children).
     * Only deletes platform categories (tenantId=0).
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long categoryId) {
        ProductCategory category = productCategoryMapper.selectOne(
                new LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getId, categoryId)
                        .eq(ProductCategory::getTenantId, 0L));
        if (category == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        // Check if has children
        Long childCount = productCategoryMapper.selectCount(
                new LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getParentId, categoryId)
                        .eq(ProductCategory::getTenantId, 0L));
        if (childCount > 0) {
            throw new BusinessException("该分类下有子分类，无法删除");
        }

        productCategoryMapper.delete(
                new LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getId, categoryId)
                        .eq(ProductCategory::getTenantId, 0L));
    }

    private CategoryTreeVO toCategoryTreeVO(ProductCategory category) {
        CategoryTreeVO vo = new CategoryTreeVO();
        vo.setId(category.getId());
        vo.setParentId(category.getParentId());
        vo.setCategoryName(category.getCategoryName());
        vo.setIcon(category.getIcon());
        vo.setImage(category.getImage());
        vo.setSortOrder(category.getSortOrder());
        vo.setLevel(category.getLevel());
        vo.setStatus(category.getStatus());
        return vo;
    }

    private List<CategoryTreeVO> buildTree(List<CategoryTreeVO> allNodes) {
        Map<Long, List<CategoryTreeVO>> groupedByParent = allNodes.stream()
                .collect(Collectors.groupingBy(CategoryTreeVO::getParentId));

        allNodes.forEach(node -> node.setChildren(
                groupedByParent.getOrDefault(node.getId(), new ArrayList<>())));

        return allNodes.stream()
                .filter(node -> node.getParentId() == 0)
                .collect(Collectors.toList());
    }
}
