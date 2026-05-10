package com.mall.service.points;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.points.PointsProductCategoryMapper;
import com.mall.dao.mapper.points.PointsProductMapper;
import com.mall.model.dto.points.PointsProductCreateDTO;
import com.mall.model.entity.points.PointsProduct;
import com.mall.model.vo.points.PointsProductDetailVO;
import com.mall.model.vo.points.PointsProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointsProductService {

    private final PointsProductMapper pointsProductMapper;
    private final PointsProductCategoryMapper pointsProductCategoryMapper;

    public PageResult<PointsProductVO> list(Long categoryId, Integer status, int page, int limit) {
        Page<PointsProduct> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<PointsProduct> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(PointsProduct::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(PointsProduct::getStatus, status);
        }
        wrapper.orderByAsc(PointsProduct::getSortOrder).orderByDesc(PointsProduct::getCreateTime);
        Page<PointsProduct> result = pointsProductMapper.selectPage(pageParam, wrapper);
        List<PointsProductVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    public PointsProductDetailVO detail(Long id) {
        PointsProduct product = pointsProductMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        PointsProductDetailVO vo = new PointsProductDetailVO();
        BeanUtils.copyProperties(product, vo);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(Long tenantId, PointsProductCreateDTO dto) {
        PointsProduct product = new PointsProduct();
        BeanUtils.copyProperties(dto, product);
        product.setTenantId(tenantId);
        product.setSales(0);
        product.setStatus(1);
        pointsProductMapper.insert(product);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PointsProductCreateDTO dto) {
        PointsProduct product = pointsProductMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        BeanUtils.copyProperties(dto, product);
        product.setId(id);
        pointsProductMapper.updateById(product);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PointsProduct product = pointsProductMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        pointsProductMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        PointsProduct product = pointsProductMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        product.setStatus(status);
        pointsProductMapper.updateById(product);
    }

    private PointsProductVO toVO(PointsProduct product) {
        PointsProductVO vo = new PointsProductVO();
        vo.setId(product.getId());
        vo.setCategoryId(product.getCategoryId());
        vo.setProductName(product.getProductName());
        vo.setProductImage(product.getProductImage());
        vo.setExchangeType(product.getExchangeType());
        vo.setPointsPrice(product.getPointsPrice());
        vo.setCashPrice(product.getCashPrice());
        vo.setStock(product.getStock());
        vo.setSales(product.getSales());
        vo.setStatus(product.getStatus());
        return vo;
    }
}
