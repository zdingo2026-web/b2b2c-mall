package com.mall.service.decoration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.decoration.DecoTemplateMapper;
import com.mall.model.dto.decoration.DecoTemplateSaveDTO;
import com.mall.model.entity.decoration.DecoTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DecoTemplateService {

    private final DecoTemplateMapper decoTemplateMapper;

    public PageResult<DecoTemplate> list(Integer pageType, int page, int limit) {
        Page<DecoTemplate> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<DecoTemplate> wrapper = new LambdaQueryWrapper<>();
        if (pageType != null) {
            wrapper.eq(DecoTemplate::getPageType, pageType);
        }
        wrapper.orderByDesc(DecoTemplate::getCreateTime);
        Page<DecoTemplate> result = decoTemplateMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, limit);
    }

    public DecoTemplate detail(Long id) {
        DecoTemplate template = decoTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return template;
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(Long tenantId, DecoTemplateSaveDTO dto) {
        DecoTemplate template = new DecoTemplate();
        template.setTemplateName(dto.getTemplateName());
        template.setTenantId(tenantId);
        template.setSource(tenantId == 0L ? 1 : 2);
        template.setPageType(dto.getPageType());
        template.setComponentList(dto.getComponentList());
        template.setComponentCount(0);
        template.setUseCount(0);
        decoTemplateMapper.insert(template);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DecoTemplate template = decoTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        decoTemplateMapper.deleteById(id);
    }
}
