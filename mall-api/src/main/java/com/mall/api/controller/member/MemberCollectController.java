package com.mall.api.controller.member;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.response.ResultCode;
import com.mall.common.util.UserContext;
import com.mall.dao.mapper.MemberCollectMapper;
import com.mall.dao.mapper.ProductSpuMapper;
import com.mall.model.entity.MemberCollect;
import com.mall.model.entity.ProductSpu;
import com.mall.model.vo.SpuVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Member collect/favorite controller (C-end).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/member/collect")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class MemberCollectController {

    private final MemberCollectMapper memberCollectMapper;
    private final ProductSpuMapper productSpuMapper;

    @GetMapping("/list")
    public R<PageResult<SpuVO>> list(
            @RequestParam(required = false) Integer collectType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long memberId = UserContext.getUserId();

        // Query collect IDs with pagination
        Page<MemberCollect> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<MemberCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberCollect::getMemberId, memberId);
        if (collectType != null) {
            wrapper.eq(MemberCollect::getCollectType, collectType);
        } else {
            wrapper.eq(MemberCollect::getCollectType, 1);
        }
        wrapper.orderByDesc(MemberCollect::getCreateTime);
        Page<MemberCollect> result = memberCollectMapper.selectPage(pageParam, wrapper);

        // Map to SPU VO
        List<SpuVO> spuVOList = result.getRecords().stream().map(collect -> {
            ProductSpu spu = productSpuMapper.selectById(collect.getSpuId());
            if (spu == null) {
                return null;
            }
            SpuVO vo = new SpuVO();
            vo.setId(spu.getId());
            vo.setCategoryId(spu.getCategoryId());
            vo.setBrandId(spu.getBrandId());
            vo.setProductName(spu.getProductName());
            vo.setSubTitle(spu.getSubTitle());
            vo.setMainImage(spu.getMainImage());
            vo.setMinPrice(spu.getMinPrice());
            vo.setMaxPrice(spu.getMaxPrice());
            vo.setTotalStock(spu.getTotalStock());
            vo.setTotalSales(spu.getTotalSales());
            vo.setStatus(spu.getStatus());
            vo.setTenantId(spu.getTenantId());
            vo.setCreateTime(spu.getCreateTime());
            return vo;
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());

        return R.ok(PageResult.of(spuVOList, result.getTotal(), page, limit));
    }

    @PostMapping("/toggle")
    public R<Map<String, Object>> toggle(@RequestBody Map<String, Object> params) {
        Long memberId = UserContext.getUserId();
        Long spuId = params.get("spuId") != null ? Long.valueOf(params.get("spuId").toString()) : null;
        Integer collectType = params.get("collectType") != null ? Integer.valueOf(params.get("collectType").toString()) : 1;
        if (spuId == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }

        Map<String, Object> data = new HashMap<>();
        LambdaQueryWrapper<MemberCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberCollect::getMemberId, memberId)
                .eq(MemberCollect::getSpuId, spuId)
                .eq(MemberCollect::getCollectType, collectType);
        MemberCollect existing = memberCollectMapper.selectOne(wrapper);

        if (existing != null) {
            memberCollectMapper.deleteById(existing.getId());
            data.put("collected", false);
        } else {
            MemberCollect collect = new MemberCollect();
            collect.setMemberId(memberId);
            collect.setSpuId(spuId);
            collect.setCollectType(collectType);
            memberCollectMapper.insert(collect);
            data.put("collected", true);
        }
        return R.ok(data);
    }
}
