package com.mall.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.TenantCategoryMapper;
import com.mall.model.dto.TenantCategoryDTO;
import com.mall.model.entity.TenantCategory;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Tenant category service.
 * Handles CRUD for merchant categories (platform-level, no tenant isolation).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantCategoryService {

    private final TenantCategoryMapper tenantCategoryMapper;

    /**
     * List all tenant categories (flat list, ordered by sort).
     */
    public List<TenantCategory> list() {
        return tenantCategoryMapper.selectList(
                new LambdaQueryWrapper<TenantCategory>()
                        .orderByAsc(TenantCategory::getSort)
                        .orderByDesc(TenantCategory::getCreateTime));
    }

    /**
     * Create a tenant category.
     */
    @Transactional(rollbackFor = Exception.class)
    public Long create(TenantCategoryDTO dto) {
        TenantCategory category = new TenantCategory();
        category.setParentId(dto.getParentId());
        category.setName(dto.getName());
        category.setIcon(dto.getIcon());
        category.setSort(dto.getSort() != null ? dto.getSort() : 0);
        category.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        tenantCategoryMapper.insert(category);
        return category.getId();
    }

    /**
     * Update a tenant category.
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, TenantCategoryDTO dto) {
        TenantCategory category = tenantCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (dto.getParentId() != null) {
            category.setParentId(dto.getParentId());
        }
        if (dto.getName() != null) {
            category.setName(dto.getName());
        }
        if (dto.getIcon() != null) {
            category.setIcon(dto.getIcon());
        }
        if (dto.getSort() != null) {
            category.setSort(dto.getSort());
        }
        if (dto.getStatus() != null) {
            category.setStatus(dto.getStatus());
        }
        tenantCategoryMapper.updateById(category);
    }

    /**
     * Delete a tenant category.
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        TenantCategory category = tenantCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        tenantCategoryMapper.deleteById(id);
    }
}
