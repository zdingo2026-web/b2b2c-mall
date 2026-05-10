package com.mall.service.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.dao.mapper.ProductCategoryMapper;
import com.mall.dao.mapper.tenant.CategoryCommissionMapper;
import com.mall.model.dto.tenant.CategoryCommissionDTO;
import com.mall.model.entity.ProductCategory;
import com.mall.model.entity.tenant.CategoryCommission;
import com.mall.model.vo.tenant.CategoryCommissionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryCommissionService {

    private final CategoryCommissionMapper categoryCommissionMapper;
    private final ProductCategoryMapper productCategoryMapper;

    public List<CategoryCommissionVO> list() {
        List<CategoryCommission> commissions = categoryCommissionMapper.selectList(
                new LambdaQueryWrapper<CategoryCommission>());
        if (commissions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> categoryIds = commissions.stream()
                .map(CategoryCommission::getCategoryId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> categoryNameMap = productCategoryMapper.selectBatchIds(categoryIds)
                .stream()
                .collect(Collectors.toMap(ProductCategory::getId, ProductCategory::getCategoryName, (a, b) -> a));

        return commissions.stream().map(c -> {
            CategoryCommissionVO vo = new CategoryCommissionVO();
            BeanUtils.copyProperties(c, vo);
            vo.setCategoryName(categoryNameMap.get(c.getCategoryId()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchSave(CategoryCommissionDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            return;
        }
        for (CategoryCommissionDTO.CategoryCommissionItem item : dto.getItems()) {
            CategoryCommission existing = categoryCommissionMapper.selectOne(
                    new LambdaQueryWrapper<CategoryCommission>()
                            .eq(CategoryCommission::getCategoryId, item.getCategoryId()));
            if (existing != null) {
                existing.setRateLevel1(item.getRateLevel1());
                existing.setRateLevel2(item.getRateLevel2());
                existing.setRateLevel3(item.getRateLevel3());
                categoryCommissionMapper.updateById(existing);
            } else {
                CategoryCommission commission = new CategoryCommission();
                commission.setCategoryId(item.getCategoryId());
                commission.setRateLevel1(item.getRateLevel1());
                commission.setRateLevel2(item.getRateLevel2());
                commission.setRateLevel3(item.getRateLevel3());
                categoryCommissionMapper.insert(commission);
            }
        }
    }
}
