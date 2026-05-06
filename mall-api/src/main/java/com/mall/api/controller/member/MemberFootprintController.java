package com.mall.api.controller.member;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.dao.mapper.MemberFootprintMapper;
import com.mall.dao.mapper.ProductSpuMapper;
import com.mall.model.entity.MemberFootprint;
import com.mall.model.entity.ProductSpu;
import com.mall.model.vo.SpuVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Member footprint controller (C-end).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/member/footprint")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class MemberFootprintController {

    private final MemberFootprintMapper memberFootprintMapper;
    private final ProductSpuMapper productSpuMapper;

    @GetMapping("/list")
    public R<PageResult<SpuVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long memberId = UserContext.getUserId();

        Page<MemberFootprint> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<MemberFootprint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberFootprint::getMemberId, memberId)
                .orderByDesc(MemberFootprint::getCreateTime);
        Page<MemberFootprint> result = memberFootprintMapper.selectPage(pageParam, wrapper);

        List<SpuVO> spuVOList = result.getRecords().stream().map(footprint -> {
            ProductSpu spu = productSpuMapper.selectById(footprint.getSpuId());
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
        }).filter(Objects::nonNull).collect(Collectors.toList());

        return R.ok(PageResult.of(spuVOList, result.getTotal(), page, limit));
    }

    /**
     * 按日期分组的浏览历史
     */
    @GetMapping("/grouped")
    public R<List<Map<String, Object>>> grouped() {
        Long memberId = UserContext.getUserId();

        LambdaQueryWrapper<MemberFootprint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberFootprint::getMemberId, memberId)
                .orderByDesc(MemberFootprint::getCreateTime);
        List<MemberFootprint> allFootprints = memberFootprintMapper.selectList(wrapper);

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Map<String, List<MemberFootprint>> grouped = new LinkedHashMap<>();

        for (MemberFootprint fp : allFootprints) {
            LocalDate fpDate = fp.getCreateTime().toLocalDate();
            String label;
            if (fpDate.equals(today)) {
                label = "今天";
            } else if (fpDate.equals(yesterday)) {
                label = "昨天";
            } else {
                label = "更早";
            }
            grouped.computeIfAbsent(label, k -> new ArrayList<>()).add(fp);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<MemberFootprint>> entry : grouped.entrySet()) {
            Map<String, Object> group = new HashMap<>();
            group.put("label", entry.getKey());
            List<SpuVO> items = entry.getValue().stream().map(fp -> {
                ProductSpu spu = productSpuMapper.selectById(fp.getSpuId());
                if (spu == null) return null;
                SpuVO vo = new SpuVO();
                vo.setId(spu.getId());
                vo.setProductName(spu.getProductName());
                vo.setMainImage(spu.getMainImage());
                vo.setMinPrice(spu.getMinPrice());
                vo.setMaxPrice(spu.getMaxPrice());
                vo.setOriginalPrice(spu.getOriginalPrice());
                vo.setTagType(spu.getTagType());
                vo.setTotalSales(spu.getTotalSales());
                return vo;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            group.put("items", items);
            result.add(group);
        }

        return R.ok(result);
    }

    /**
     * 清空浏览历史
     */
    @DeleteMapping("/clear")
    public R<Void> clear() {
        Long memberId = UserContext.getUserId();
        LambdaQueryWrapper<MemberFootprint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberFootprint::getMemberId, memberId);
        memberFootprintMapper.delete(wrapper);
        return R.ok();
    }
}
