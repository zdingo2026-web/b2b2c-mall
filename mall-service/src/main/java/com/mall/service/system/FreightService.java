package com.mall.service.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.common.util.TenantContext;
import com.mall.dao.mapper.FreightTemplateMapper;
import com.mall.dao.mapper.MemberAddressMapper;
import com.mall.dao.mapper.ProductSkuMapper;
import com.mall.model.dto.FreightCalculateDTO;
import com.mall.model.dto.FreightTemplateSaveDTO;
import com.mall.model.entity.FreightTemplate;
import com.mall.model.entity.MemberAddress;
import com.mall.model.entity.ProductSku;
import com.mall.model.vo.FreightVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Freight service.
 * Calculates shipping fees based on freight templates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FreightService {

    private final FreightTemplateMapper freightTemplateMapper;
    private final ProductSkuMapper productSkuMapper;
    private final MemberAddressMapper memberAddressMapper;

    /**
     * Calculate freight for a list of SKUs shipped to a given address.
     *
     * @param dto freight calculate request
     * @return freight calculation result
     */
    public FreightVO calculateFreight(FreightCalculateDTO dto) {
        // Get the shipping address
        MemberAddress address = memberAddressMapper.selectById(dto.getAddressId());
        if (address == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "收货地址不存在");
        }

        // Aggregate SKU quantities, weights, volumes, and amounts
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalVolume = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (FreightCalculateDTO.SkuItem item : dto.getSkuList()) {
            ProductSku sku = productSkuMapper.selectById(item.getSkuId());
            if (sku == null) {
                continue;
            }
            int qty = item.getQuantity() != null ? item.getQuantity() : 1;
            totalQuantity = totalQuantity.add(BigDecimal.valueOf(qty));
            if (item.getPrice() != null) {
                totalAmount = totalAmount.add(item.getPrice().multiply(BigDecimal.valueOf(qty)));
            }
            if (sku.getWeight() != null) {
                totalWeight = totalWeight.add(sku.getWeight().multiply(BigDecimal.valueOf(qty)));
            }
            if (item.getVolume() != null) {
                totalVolume = totalVolume.add(item.getVolume().multiply(BigDecimal.valueOf(qty)));
            }
        }

        // Find applicable freight template (use the first active template for MVP)
        LambdaQueryWrapper<FreightTemplate> templateQuery = new LambdaQueryWrapper<>();
        templateQuery.eq(FreightTemplate::getStatus, 1)
                .last("LIMIT 1");
        FreightTemplate template = freightTemplateMapper.selectOne(templateQuery);

        if (template == null) {
            // No template found: free shipping
            FreightVO vo = new FreightVO();
            vo.setFreightAmount(BigDecimal.ZERO);
            vo.setFreeShipping(true);
            vo.setTemplateName("默认免运费");
            vo.setChargeTypeDesc("免运费");
            return vo;
        }

        // Check free shipping threshold
        if (template.getFreeAmount() != null
                && template.getFreeAmount().compareTo(BigDecimal.ZERO) > 0
                && totalAmount.compareTo(template.getFreeAmount()) >= 0) {
            FreightVO vo = new FreightVO();
            vo.setFreightAmount(BigDecimal.ZERO);
            vo.setFreeShipping(true);
            vo.setTemplateName(template.getTemplateName());
            vo.setChargeTypeDesc(getChargeTypeDesc(template.getChargeType()));
            return vo;
        }

        // Calculate freight based on charge type
        BigDecimal freightAmount = BigDecimal.ZERO;
        BigDecimal baseAmount;
        BigDecimal basePrice;
        BigDecimal continueAmount;
        BigDecimal continuePrice;
        BigDecimal actualAmount;

        switch (template.getChargeType()) {
            case 1: // By quantity
                baseAmount = template.getDefaultFirstAmount();
                basePrice = template.getDefaultFirstPrice();
                continueAmount = template.getDefaultContinueAmount();
                continuePrice = template.getDefaultContinuePrice();
                actualAmount = totalQuantity;
                break;
            case 2: // By weight
                baseAmount = template.getDefaultFirstAmount();
                basePrice = template.getDefaultFirstPrice();
                continueAmount = template.getDefaultContinueAmount();
                continuePrice = template.getDefaultContinuePrice();
                actualAmount = totalWeight;
                break;
            case 3: // By volume
                baseAmount = template.getDefaultFirstAmount();
                basePrice = template.getDefaultFirstPrice();
                continueAmount = template.getDefaultContinueAmount();
                continuePrice = template.getDefaultContinuePrice();
                actualAmount = totalVolume;
                break;
            default:
                baseAmount = BigDecimal.ONE;
                basePrice = BigDecimal.ZERO;
                continueAmount = BigDecimal.ONE;
                continuePrice = BigDecimal.ZERO;
                actualAmount = totalQuantity;
        }

        // First unit price
        freightAmount = freightAmount.add(basePrice);
        // Remaining units price
        if (actualAmount.compareTo(baseAmount) > 0
                && continueAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal remaining = actualAmount.subtract(baseAmount);
            BigDecimal additionalUnits = remaining.divide(continueAmount, 0, BigDecimal.ROUND_UP);
            freightAmount = freightAmount.add(additionalUnits.multiply(continuePrice));
        }

        FreightVO vo = new FreightVO();
        vo.setFreightAmount(freightAmount);
        vo.setFreeShipping(false);
        vo.setTemplateName(template.getTemplateName());
        vo.setChargeTypeDesc(getChargeTypeDesc(template.getChargeType()));
        return vo;
    }

    /**
     * Get freight template by ID (admin).
     *
     * @param id template ID
     * @return FreightTemplate entity
     */
    public FreightTemplate getById(Long id) {
        FreightTemplate template = freightTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return template;
    }

    /**
     * List all freight templates (admin).
     *
     * @return list of FreightTemplate
     */
    public List<FreightTemplate> listAll() {
        LambdaQueryWrapper<FreightTemplate> query = new LambdaQueryWrapper<>();
        query.orderByDesc(FreightTemplate::getCreateTime);
        return freightTemplateMapper.selectList(query);
    }

    /**
     * Create a freight template (admin).
     *
     * @param dto freight template save DTO
     * @return created template ID
     */
    public Long create(FreightTemplateSaveDTO dto) {
        FreightTemplate template = new FreightTemplate();
        BeanUtils.copyProperties(dto, template);
        template.setTenantId(TenantContext.getTenantId());
        if (template.getStatus() == null) {
            template.setStatus(1);
        }
        freightTemplateMapper.insert(template);
        return template.getId();
    }

    /**
     * Update a freight template (admin).
     *
     * @param id  template ID
     * @param dto freight template save DTO
     */
    public void update(Long id, FreightTemplateSaveDTO dto) {
        FreightTemplate template = freightTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        BeanUtils.copyProperties(dto, template);
        template.setId(id);
        freightTemplateMapper.updateById(template);
    }

    /**
     * Delete a freight template (admin).
     *
     * @param id template ID
     */
    public void delete(Long id) {
        FreightTemplate template = freightTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        freightTemplateMapper.deleteById(id);
    }

    private String getChargeTypeDesc(Integer chargeType) {
        switch (chargeType) {
            case 1:
                return "按件数计费";
            case 2:
                return "按重量计费";
            case 3:
                return "按体积计费";
            default:
                return "未知计费方式";
        }
    }
}
