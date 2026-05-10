package com.mall.service.distribution;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.ProductSpuMapper;
import com.mall.dao.mapper.distribution.DistributionConfigMapper;
import com.mall.dao.mapper.distribution.DistributionProductMapper;
import com.mall.model.dto.distribution.DistributionProductDTO;
import com.mall.model.entity.ProductSpu;
import com.mall.model.entity.distribution.DistributionConfig;
import com.mall.model.entity.distribution.DistributionProduct;
import com.mall.model.vo.distribution.DistributionProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributionProductService {

    private final DistributionProductMapper distributionProductMapper;
    private final ProductSpuMapper productSpuMapper;
    private final DistributionConfigMapper distributionConfigMapper;

    public PageResult<DistributionProductVO> list(Long tenantId, String keyword, int page, int limit) {
        Page<ProductSpu> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<ProductSpu> wrapper = new LambdaQueryWrapper<ProductSpu>()
                .eq(ProductSpu::getTenantId, tenantId)
                .eq(ProductSpu::getStatus, 1);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(ProductSpu::getProductName, keyword);
        }
        wrapper.orderByDesc(ProductSpu::getCreateTime);

        Page<ProductSpu> result = productSpuMapper.selectPage(pageParam, wrapper);
        List<DistributionProductVO> voList = result.getRecords().stream()
                .map(spu -> {
                    DistributionProductVO vo = new DistributionProductVO();
                    vo.setSpuId(spu.getId());
                    vo.setSpuName(spu.getProductName());
                    vo.setMainImage(spu.getMainImage());
                    vo.setPrice(spu.getMinPrice());

                    DistributionProduct dp = distributionProductMapper.selectOne(
                            new LambdaQueryWrapper<DistributionProduct>()
                                    .eq(DistributionProduct::getSpuId, spu.getId()));
                    if (dp != null) {
                        vo.setId(dp.getId());
                        vo.setUseGlobal(dp.getUseGlobal() != null && dp.getUseGlobal() == 1);
                        vo.setRateLevel1(dp.getRateLevel1());
                        vo.setRateLevel2(dp.getRateLevel2());
                        vo.setRateLevel3(dp.getRateLevel3());
                        vo.setCanDistribute(dp.getCanDistribute() != null && dp.getCanDistribute() == 1);
                    } else {
                        vo.setUseGlobal(true);
                        vo.setCanDistribute(true);
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Long spuId, DistributionProductDTO dto) {
        DistributionProduct dp = distributionProductMapper.selectOne(
                new LambdaQueryWrapper<DistributionProduct>()
                        .eq(DistributionProduct::getSpuId, spuId));
        if (dp == null) {
            dp = new DistributionProduct();
            dp.setSpuId(spuId);
        }
        dp.setUseGlobal(dto.getUseGlobal() != null && dto.getUseGlobal() ? 1 : 0);
        dp.setCanDistribute(dto.getCanDistribute() != null && dto.getCanDistribute() ? 1 : 0);

        if (!dp.getUseGlobal().equals(1)) {
            dp.setRateLevel1(dto.getRateLevel1());
            dp.setRateLevel2(dto.getRateLevel2());
            dp.setRateLevel3(dto.getRateLevel3());
        } else {
            DistributionConfig config = distributionConfigMapper.selectOne(
                    new LambdaQueryWrapper<DistributionConfig>().last("LIMIT 1"));
            if (config != null) {
                dp.setRateLevel1(config.getRateLevel1());
                dp.setRateLevel2(config.getRateLevel2());
                dp.setRateLevel3(config.getRateLevel3());
            }
        }

        if (dp.getId() == null) {
            distributionProductMapper.insert(dp);
        } else {
            distributionProductMapper.updateById(dp);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchSetDistribution(List<Long> spuIds, Boolean canDistribute) {
        for (Long spuId : spuIds) {
            DistributionProduct dp = distributionProductMapper.selectOne(
                    new LambdaQueryWrapper<DistributionProduct>()
                            .eq(DistributionProduct::getSpuId, spuId));
            if (dp == null) {
                dp = new DistributionProduct();
                dp.setSpuId(spuId);
                dp.setUseGlobal(1);
                dp.setCanDistribute(canDistribute ? 1 : 0);
                DistributionConfig config = distributionConfigMapper.selectOne(
                        new LambdaQueryWrapper<DistributionConfig>().last("LIMIT 1"));
                if (config != null) {
                    dp.setRateLevel1(config.getRateLevel1());
                    dp.setRateLevel2(config.getRateLevel2());
                    dp.setRateLevel3(config.getRateLevel3());
                }
                distributionProductMapper.insert(dp);
            } else {
                dp.setCanDistribute(canDistribute ? 1 : 0);
                distributionProductMapper.updateById(dp);
            }
        }
    }
}
