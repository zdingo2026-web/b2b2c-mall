package com.mall.service.decoration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.constant.CommonConstant;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.decoration.DecoPageMapper;
import com.mall.model.dto.decoration.DecoPageSaveDTO;
import com.mall.model.entity.decoration.DecoPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DecoPageService {

    private final DecoPageMapper decoPageMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String PAGE_CACHE_PREFIX = CommonConstant.REDIS_KEY_PREFIX + "deco:page:";
    private static final long PAGE_CACHE_TTL_MINUTES = 30;

    public PageResult<DecoPage> list(Long tenantId, Integer pageType, int page, int limit) {
        Page<DecoPage> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<DecoPage> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(DecoPage::getTenantId, tenantId);
        }
        if (pageType != null) {
            wrapper.eq(DecoPage::getPageType, pageType);
        }
        wrapper.orderByDesc(DecoPage::getCreateTime);
        Page<DecoPage> result = decoPageMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, limit);
    }

    public DecoPage detail(Long id) {
        DecoPage decoPage = decoPageMapper.selectById(id);
        if (decoPage == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return decoPage;
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(Long tenantId, DecoPageSaveDTO dto) {
        DecoPage page = new DecoPage();
        page.setTenantId(tenantId);
        page.setPageType(dto.getPageType());
        page.setPageName(dto.getPageName());
        page.setComponentList(dto.getComponentList());
        page.setDraftJson(dto.getComponentList());
        page.setIsPublished(0);
        decoPageMapper.insert(page);
    }

    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        DecoPage page = decoPageMapper.selectById(id);
        if (page == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        page.setPublishedJson(page.getDraftJson());
        page.setIsPublished(1);
        page.setPublishTime(LocalDateTime.now());
        decoPageMapper.updateById(page);

        String cacheKey = PAGE_CACHE_PREFIX + page.getTenantId() + ":" + page.getPageType();
        stringRedisTemplate.delete(cacheKey);
    }

    public String getPublishedPage(Long tenantId, Integer pageType) {
        String cacheKey = PAGE_CACHE_PREFIX + tenantId + ":" + pageType;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        DecoPage page = decoPageMapper.selectOne(
                new LambdaQueryWrapper<DecoPage>()
                        .eq(DecoPage::getTenantId, tenantId)
                        .eq(DecoPage::getPageType, pageType)
                        .eq(DecoPage::getIsPublished, 1)
                        .last("LIMIT 1"));
        if (page == null || page.getPublishedJson() == null) {
            return null;
        }

        stringRedisTemplate.opsForValue().set(cacheKey, page.getPublishedJson(),
                PAGE_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return page.getPublishedJson();
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DecoPage page = decoPageMapper.selectById(id);
        if (page == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        decoPageMapper.deleteById(id);

        if (page.getIsPublished() == 1) {
            String cacheKey = PAGE_CACHE_PREFIX + page.getTenantId() + ":" + page.getPageType();
            stringRedisTemplate.delete(cacheKey);
        }
    }
}
