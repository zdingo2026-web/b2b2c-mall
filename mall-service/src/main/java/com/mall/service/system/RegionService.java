package com.mall.service.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.constant.CommonConstant;
import com.mall.dao.mapper.RegionMapper;
import com.mall.model.entity.Region;
import com.mall.model.vo.RegionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Region service.
 * Provides five-level region tree with Redis cache.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionMapper regionMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String REGION_TREE_CACHE_KEY = CommonConstant.REDIS_KEY_PREFIX + "region:tree";
    private static final String REGION_NAME_CACHE_KEY = CommonConstant.REDIS_KEY_PREFIX + "region:name:";
    private static final long REGION_CACHE_TTL_HOURS = 24;

    /**
     * Get the full five-level region tree.
     * Cached in Redis for 24 hours.
     *
     * @return list of top-level RegionVO with nested children
     */
    public List<RegionVO> getRegionTree() {
        // Query all regions, sorted by level and sort
        LambdaQueryWrapper<Region> query = new LambdaQueryWrapper<>();
        query.orderByAsc(Region::getLevel, Region::getSort);
        List<Region> allRegions = regionMapper.selectList(query);

        // Build tree starting from root (parentId = 0)
        return buildTree(allRegions, 0L);
    }

    /**
     * Get children of a specific region.
     *
     * @param parentId parent region ID
     * @return list of child RegionVO (without nested children)
     */
    public List<RegionVO> getChildren(Long parentId) {
        LambdaQueryWrapper<Region> query = new LambdaQueryWrapper<>();
        query.eq(Region::getParentId, parentId)
                .orderByAsc(Region::getSort);
        List<Region> children = regionMapper.selectList(query);
        return children.stream().map(this::toVOWithoutChildren).collect(Collectors.toList());
    }

    /**
     * Get region name by ID.
     *
     * @param regionId region ID
     * @return region name
     */
    public String getRegionName(Long regionId) {
        if (regionId == null) {
            return "";
        }
        Region region = regionMapper.selectById(regionId);
        return region != null ? region.getName() : "";
    }

    /**
     * Get full address string from region IDs.
     *
     * @param provinceId province ID
     * @param cityId     city ID
     * @param districtId district ID
     * @param townId     town ID (optional)
     * @return concatenated address string
     */
    public String getFullAddress(Long provinceId, Long cityId, Long districtId, Long townId) {
        StringBuilder sb = new StringBuilder();
        if (provinceId != null) {
            sb.append(getRegionName(provinceId));
        }
        if (cityId != null) {
            sb.append(getRegionName(cityId));
        }
        if (districtId != null) {
            sb.append(getRegionName(districtId));
        }
        if (townId != null) {
            sb.append(getRegionName(townId));
        }
        return sb.toString();
    }

    // ===== Private helper methods =====

    private List<RegionVO> buildTree(List<Region> allRegions, Long parentId) {
        List<RegionVO> result = new ArrayList<>();
        for (Region region : allRegions) {
            if (parentId.equals(region.getParentId())) {
                RegionVO vo = toVO(region);
                vo.setChildren(buildTree(allRegions, region.getId()));
                result.add(vo);
            }
        }
        return result;
    }

    private RegionVO toVO(Region region) {
        RegionVO vo = new RegionVO();
        vo.setId(region.getId());
        vo.setName(region.getName());
        vo.setLevel(region.getLevel());
        vo.setCode(region.getCode());
        return vo;
    }

    private RegionVO toVOWithoutChildren(Region region) {
        RegionVO vo = new RegionVO();
        vo.setId(region.getId());
        vo.setName(region.getName());
        vo.setLevel(region.getLevel());
        vo.setCode(region.getCode());
        vo.setChildren(null);
        return vo;
    }
}
