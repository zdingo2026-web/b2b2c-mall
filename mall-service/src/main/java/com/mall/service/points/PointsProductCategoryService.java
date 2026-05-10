package com.mall.service.points;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.points.PointsProductCategoryMapper;
import com.mall.model.entity.points.PointsProductCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointsProductCategoryService {

    private final PointsProductCategoryMapper pointsProductCategoryMapper;

    public List<PointsProductCategory> list() {
        LambdaQueryWrapper<PointsProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(PointsProductCategory::getSortOrder);
        return pointsProductCategoryMapper.selectList(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(PointsProductCategory category) {
        if (category.getId() == null) {
            pointsProductCategoryMapper.insert(category);
        } else {
            pointsProductCategoryMapper.updateById(category);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PointsProductCategory category = pointsProductCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        pointsProductCategoryMapper.deleteById(id);
    }
}
