package com.mall.service.promotion;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.ProductSkuMapper;
import com.mall.dao.mapper.ProductSpuMapper;
import com.mall.dao.mapper.promotion.GrouponActivityMapper;
import com.mall.dao.mapper.promotion.GrouponProductMapper;
import com.mall.model.dto.promotion.GrouponCreateDTO;
import com.mall.model.dto.promotion.GrouponProductDTO;
import com.mall.model.entity.ProductSku;
import com.mall.model.entity.ProductSpu;
import com.mall.model.entity.promotion.GrouponActivity;
import com.mall.model.entity.promotion.GrouponProduct;
import com.mall.model.vo.promotion.GrouponActivityVO;
import com.mall.model.vo.promotion.GrouponProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrouponActivityService {

    private final GrouponActivityMapper grouponActivityMapper;
    private final GrouponProductMapper grouponProductMapper;
    private final ProductSpuMapper productSpuMapper;
    private final ProductSkuMapper productSkuMapper;

    public PageResult<GrouponActivityVO> list(Long tenantId, Integer status, int page, int limit) {
        Page<GrouponActivity> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<GrouponActivity> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(GrouponActivity::getTenantId, tenantId);
        }
        if (status != null) {
            wrapper.eq(GrouponActivity::getStatus, status);
        }
        wrapper.orderByDesc(GrouponActivity::getCreateTime);
        Page<GrouponActivity> result = grouponActivityMapper.selectPage(pageParam, wrapper);
        List<GrouponActivityVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(Long tenantId, GrouponCreateDTO dto) {
        GrouponActivity activity = new GrouponActivity();
        activity.setActivityName(dto.getActivityName());
        activity.setSpuId(dto.getSpuId());
        activity.setGrouponPrice(dto.getGrouponPrice());
        activity.setLimitPerUser(dto.getLimitPerUser());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setStatus(0);
        activity.setTenantId(tenantId);
        grouponActivityMapper.insert(activity);

        for (GrouponProductDTO skuDTO : dto.getSkuList()) {
            GrouponProduct product = new GrouponProduct();
            product.setActivityId(activity.getId());
            product.setSpuId(dto.getSpuId());
            product.setSkuId(skuDTO.getSkuId());
            product.setGrouponPrice(skuDTO.getGrouponPrice());
            product.setStock(skuDTO.getStock());
            product.setSales(0);
            product.setSortOrder(skuDTO.getSortOrder() != null ? skuDTO.getSortOrder() : 0);
            product.setTenantId(tenantId);
            grouponProductMapper.insert(product);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, GrouponCreateDTO dto) {
        GrouponActivity activity = grouponActivityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        activity.setActivityName(dto.getActivityName());
        activity.setSpuId(dto.getSpuId());
        activity.setGrouponPrice(dto.getGrouponPrice());
        activity.setLimitPerUser(dto.getLimitPerUser());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        grouponActivityMapper.updateById(activity);

        grouponProductMapper.delete(
                new LambdaQueryWrapper<GrouponProduct>().eq(GrouponProduct::getActivityId, id));
        for (GrouponProductDTO skuDTO : dto.getSkuList()) {
            GrouponProduct product = new GrouponProduct();
            product.setActivityId(id);
            product.setSpuId(dto.getSpuId());
            product.setSkuId(skuDTO.getSkuId());
            product.setGrouponPrice(skuDTO.getGrouponPrice());
            product.setStock(skuDTO.getStock());
            product.setSales(0);
            product.setSortOrder(skuDTO.getSortOrder() != null ? skuDTO.getSortOrder() : 0);
            product.setTenantId(activity.getTenantId());
            grouponProductMapper.insert(product);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        GrouponActivity activity = grouponActivityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        grouponProductMapper.delete(
                new LambdaQueryWrapper<GrouponProduct>().eq(GrouponProduct::getActivityId, id));
        grouponActivityMapper.deleteById(id);
    }

    public PageResult<GrouponProductVO> products(int page, int limit) {
        Page<GrouponActivity> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<GrouponActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GrouponActivity::getStatus, 1);
        wrapper.ge(GrouponActivity::getEndTime, LocalDateTime.now());
        wrapper.orderByDesc(GrouponActivity::getCreateTime);
        Page<GrouponActivity> result = grouponActivityMapper.selectPage(pageParam, wrapper);

        List<GrouponProductVO> voList = result.getRecords().stream().map(activity -> {
            GrouponProductVO vo = new GrouponProductVO();
            vo.setActivityId(activity.getId());
            vo.setActivityName(activity.getActivityName());
            vo.setSpuId(activity.getSpuId());
            vo.setGrouponPrice(activity.getGrouponPrice());
            vo.setLimitPerUser(activity.getLimitPerUser());
            vo.setEndTime(activity.getEndTime());

            ProductSpu spu = productSpuMapper.selectById(activity.getSpuId());
            if (spu != null) {
                vo.setSpuName(spu.getProductName());
                vo.setMainImage(spu.getMainImage());
                vo.setOriginalPrice(spu.getMinPrice());
            }
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    private GrouponActivityVO toVO(GrouponActivity activity) {
        GrouponActivityVO vo = new GrouponActivityVO();
        vo.setId(activity.getId());
        vo.setActivityName(activity.getActivityName());
        vo.setSpuId(activity.getSpuId());
        vo.setGrouponPrice(activity.getGrouponPrice());
        vo.setLimitPerUser(activity.getLimitPerUser());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setStatus(activity.getStatus());
        return vo;
    }
}
