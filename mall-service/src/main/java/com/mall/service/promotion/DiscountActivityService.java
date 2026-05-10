package com.mall.service.promotion;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.ProductSpuMapper;
import com.mall.dao.mapper.promotion.DiscountActivityMapper;
import com.mall.dao.mapper.promotion.DiscountProductMapper;
import com.mall.model.entity.ProductSpu;
import com.mall.model.entity.promotion.DiscountActivity;
import com.mall.model.entity.promotion.DiscountProduct;
import com.mall.model.vo.promotion.DiscountActivityVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountActivityService {

    private final DiscountActivityMapper discountActivityMapper;
    private final DiscountProductMapper discountProductMapper;
    private final ProductSpuMapper productSpuMapper;

    public PageResult<DiscountActivityVO> list(Long tenantId, Integer status, int page, int limit) {
        Page<DiscountActivity> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<DiscountActivity> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(DiscountActivity::getTenantId, tenantId);
        }
        if (status != null) {
            wrapper.eq(DiscountActivity::getStatus, status);
        }
        wrapper.orderByDesc(DiscountActivity::getCreateTime);
        Page<DiscountActivity> result = discountActivityMapper.selectPage(pageParam, wrapper);
        List<DiscountActivityVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(Long tenantId, DiscountActivity activity, List<Long> spuIds) {
        activity.setTenantId(tenantId);
        activity.setStatus(0);
        discountActivityMapper.insert(activity);

        for (Long spuId : spuIds) {
            DiscountProduct product = new DiscountProduct();
            product.setActivityId(activity.getId());
            product.setSpuId(spuId);
            product.setTenantId(tenantId);
            discountProductMapper.insert(product);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, DiscountActivity activity, List<Long> spuIds) {
        DiscountActivity existing = discountActivityMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        existing.setActivityName(activity.getActivityName());
        existing.setDiscountType(activity.getDiscountType());
        existing.setDiscountValue(activity.getDiscountValue());
        existing.setMaxDiscount(activity.getMaxDiscount());
        existing.setStartTime(activity.getStartTime());
        existing.setEndTime(activity.getEndTime());
        discountActivityMapper.updateById(existing);

        discountProductMapper.delete(
                new LambdaQueryWrapper<DiscountProduct>().eq(DiscountProduct::getActivityId, id));
        for (Long spuId : spuIds) {
            DiscountProduct product = new DiscountProduct();
            product.setActivityId(id);
            product.setSpuId(spuId);
            product.setTenantId(existing.getTenantId());
            discountProductMapper.insert(product);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DiscountActivity activity = discountActivityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        discountProductMapper.delete(
                new LambdaQueryWrapper<DiscountProduct>().eq(DiscountProduct::getActivityId, id));
        discountActivityMapper.deleteById(id);
    }

    private DiscountActivityVO toVO(DiscountActivity activity) {
        DiscountActivityVO vo = new DiscountActivityVO();
        vo.setId(activity.getId());
        vo.setActivityName(activity.getActivityName());
        vo.setDiscountType(activity.getDiscountType());
        vo.setDiscountValue(activity.getDiscountValue());
        vo.setMaxDiscount(activity.getMaxDiscount());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setStatus(activity.getStatus());
        return vo;
    }
}
