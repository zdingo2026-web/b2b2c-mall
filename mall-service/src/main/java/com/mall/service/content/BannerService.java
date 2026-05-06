package com.mall.service.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.constant.CommonConstant;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.common.util.TenantContext;
import com.mall.dao.mapper.ContentBannerMapper;
import com.mall.model.dto.BannerSaveDTO;
import com.mall.model.entity.ContentBanner;
import com.mall.model.vo.BannerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Banner service.
 * C-end: query valid banners with Redis cache.
 * Admin: CRUD operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BannerService {

    private final ContentBannerMapper contentBannerMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String BANNER_CACHE_KEY = CommonConstant.REDIS_KEY_PREFIX + "banner:list";
    private static final long BANNER_CACHE_TTL_MINUTES = 10;

    /**
     * Get banner list for C-end (valid banners only, Redis cached for 10 minutes).
     *
     * @return list of BannerVO
     */
    public List<BannerVO> getBannerList() {
        // Query valid banners: status=1, within time range, sorted by sort asc
        LambdaQueryWrapper<ContentBanner> query = new LambdaQueryWrapper<>();
        query.eq(ContentBanner::getStatus, 1)
                .le(ContentBanner::getStartTime, LocalDateTime.now())
                .ge(ContentBanner::getEndTime, LocalDateTime.now())
                .orderByAsc(ContentBanner::getSort);

        List<ContentBanner> banners = contentBannerMapper.selectList(query);
        return banners.stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * Get banner by ID (admin).
     *
     * @param id banner ID
     * @return ContentBanner entity
     */
    public ContentBanner getById(Long id) {
        ContentBanner banner = contentBannerMapper.selectById(id);
        if (banner == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return banner;
    }

    /**
     * Get banner list for admin (all banners including disabled).
     *
     * @return list of ContentBanner
     */
    public List<ContentBanner> listAll() {
        LambdaQueryWrapper<ContentBanner> query = new LambdaQueryWrapper<>();
        query.orderByAsc(ContentBanner::getSort);
        return contentBannerMapper.selectList(query);
    }

    /**
     * Create a banner (admin).
     *
     * @param dto banner save DTO
     * @return created banner ID
     */
    public Long create(BannerSaveDTO dto) {
        ContentBanner banner = new ContentBanner();
        BeanUtils.copyProperties(dto, banner);
        if (banner.getSort() == null) {
            banner.setSort(0);
        }
        if (banner.getStatus() == null) {
            banner.setStatus(1);
        }
        banner.setTenantId(TenantContext.getTenantId());
        contentBannerMapper.insert(banner);
        clearBannerCache();
        return banner.getId();
    }

    /**
     * Update a banner (admin).
     *
     * @param id  banner ID
     * @param dto banner save DTO
     */
    public void update(Long id, BannerSaveDTO dto) {
        ContentBanner banner = contentBannerMapper.selectById(id);
        if (banner == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        BeanUtils.copyProperties(dto, banner);
        banner.setId(id);
        contentBannerMapper.updateById(banner);
        clearBannerCache();
    }

    /**
     * Delete a banner (admin).
     *
     * @param id banner ID
     */
    public void delete(Long id) {
        ContentBanner banner = contentBannerMapper.selectById(id);
        if (banner == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        contentBannerMapper.deleteById(id);
        clearBannerCache();
    }

    private BannerVO toVO(ContentBanner banner) {
        BannerVO vo = new BannerVO();
        vo.setId(banner.getId());
        vo.setTitle(banner.getTitle());
        vo.setImageUrl(banner.getImageUrl());
        vo.setLinkUrl(banner.getLinkUrl());
        vo.setLinkType(banner.getLinkType());
        return vo;
    }

    private void clearBannerCache() {
        stringRedisTemplate.delete(BANNER_CACHE_KEY);
    }
}
