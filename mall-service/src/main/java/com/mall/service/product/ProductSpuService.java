package com.mall.service.product;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.enums.OrderStatusEnum;
import com.mall.common.enums.ProductStatusEnum;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.common.util.HtmlSanitizeUtil;
import com.mall.common.util.SnowflakeIdUtil;
import com.mall.dao.mapper.*;
import com.mall.model.dto.CommentCreateDTO;
import com.mall.model.dto.SpuCreateDTO;
import com.mall.model.dto.SpuQueryDTO;
import com.mall.model.entity.*;
import com.mall.model.vo.SpuDetailVO;
import com.mall.model.vo.SpuVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Product SPU service.
 * Handles SPU+SKU creation, listing, detail, publish/unpublish, audit.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSpuService {

    private final ProductSpuMapper productSpuMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductAttributeValueMapper productAttributeValueMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductBrandMapper productBrandMapper;
    private final TenantMapper tenantMapper;
    private final OrderMainMapper orderMainMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductCommentMapper productCommentMapper;

    /**
     * Create SPU + SKU.
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createSpu(SpuCreateDTO dto, Long tenantId) {
        ProductSpu spu = new ProductSpu();
        spu.setTenantId(tenantId);
        spu.setCategoryId(dto.getCategoryId());
        spu.setBrandId(dto.getBrandId());
        spu.setProductName(dto.getProductName());
        spu.setSubTitle(dto.getSubTitle());
        spu.setMainImage(dto.getMainImage());
        spu.setImages(dto.getImages() != null ? String.join(",", dto.getImages()) : null);
        spu.setDescription(HtmlSanitizeUtil.sanitize(dto.getDescription()));
        spu.setStatus(ProductStatusEnum.DRAFT.getCode());
        spu.setAuditStatus(0);
        spu.setSortOrder(0);
        spu.setTotalSales(0);
        spu.setTagType(dto.getTagType());
        spu.setOriginalPrice(dto.getOriginalPrice());

        // Calculate price range and total stock from SKU list
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        int totalStock = 0;

        for (SpuCreateDTO.SkuItem skuItem : dto.getSkuList()) {
            if (minPrice == null || skuItem.getPrice().compareTo(minPrice) < 0) {
                minPrice = skuItem.getPrice();
            }
            if (maxPrice == null || skuItem.getPrice().compareTo(maxPrice) > 0) {
                maxPrice = skuItem.getPrice();
            }
            totalStock += skuItem.getStock();
        }

        spu.setMinPrice(minPrice);
        spu.setMaxPrice(maxPrice);
        spu.setTotalStock(totalStock);

        productSpuMapper.insert(spu);

        // Insert SKU list
        for (SpuCreateDTO.SkuItem skuItem : dto.getSkuList()) {
            ProductSku sku = new ProductSku();
            sku.setTenantId(tenantId);
            sku.setSpuId(spu.getId());
            sku.setSkuName(skuItem.getSkuName());
            sku.setSkuCode(skuItem.getSkuCode() != null ? skuItem.getSkuCode()
                    : "SKU" + SnowflakeIdUtil.getInstance().nextIdStr());
            sku.setSpecValues(skuItem.getSpecValues());
            sku.setPrice(skuItem.getPrice());
            sku.setOriginalPrice(skuItem.getOriginalPrice());
            sku.setStock(skuItem.getStock());
            sku.setLockStock(0);
            sku.setSales(0);
            sku.setImage(skuItem.getImage());
            sku.setWeight(skuItem.getWeight());
            sku.setStatus(1);
            productSkuMapper.insert(sku);
        }

        // Insert images
        if (dto.getImages() != null) {
            int sortOrder = 0;
            for (String imageUrl : dto.getImages()) {
                ProductImage image = new ProductImage();
                image.setTenantId(tenantId);
                image.setSpuId(spu.getId());
                image.setImageUrl(imageUrl);
                image.setSortOrder(sortOrder++);
                image.setImageType(sortOrder == 1 ? 1 : 2);
                productImageMapper.insert(image);
            }
        }

        // Insert attribute values
        if (dto.getAttributeList() != null) {
            for (SpuCreateDTO.AttributeItem attrItem : dto.getAttributeList()) {
                ProductAttributeValue attrValue = new ProductAttributeValue();
                attrValue.setTenantId(tenantId);
                attrValue.setSpuId(spu.getId());
                attrValue.setAttributeId(attrItem.getAttributeId());
                attrValue.setAttributeName(attrItem.getAttributeName());
                attrValue.setAttributeValue(attrItem.getAttributeValue());
                attrValue.setAttributeType(attrItem.getAttributeType());
                productAttributeValueMapper.insert(attrValue);
            }
        }

        return spu.getId();
    }

    /**
     * Update SPU.
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSpu(Long spuId, SpuCreateDTO dto, Long tenantId) {
        ProductSpu spu = productSpuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (!spu.getTenantId().equals(tenantId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        if (dto.getCategoryId() != null) {
            spu.setCategoryId(dto.getCategoryId());
        }
        if (dto.getBrandId() != null) {
            spu.setBrandId(dto.getBrandId());
        }
        if (dto.getProductName() != null) {
            spu.setProductName(dto.getProductName());
        }
        if (dto.getSubTitle() != null) {
            spu.setSubTitle(dto.getSubTitle());
        }
        if (dto.getMainImage() != null) {
            spu.setMainImage(dto.getMainImage());
        }
        if (dto.getImages() != null) {
            spu.setImages(String.join(",", dto.getImages()));
        }
        if (dto.getDescription() != null) {
            spu.setDescription(HtmlSanitizeUtil.sanitize(dto.getDescription()));
        }
        if (dto.getTagType() != null) {
            spu.setTagType(dto.getTagType());
        }
        if (dto.getOriginalPrice() != null) {
            spu.setOriginalPrice(dto.getOriginalPrice());
        }

        // Recalculate if SKU list provided
        if (dto.getSkuList() != null && !dto.getSkuList().isEmpty()) {
            BigDecimal minPrice = null;
            BigDecimal maxPrice = null;
            int totalStock = 0;

            for (SpuCreateDTO.SkuItem skuItem : dto.getSkuList()) {
                if (minPrice == null || skuItem.getPrice().compareTo(minPrice) < 0) {
                    minPrice = skuItem.getPrice();
                }
                if (maxPrice == null || skuItem.getPrice().compareTo(maxPrice) > 0) {
                    maxPrice = skuItem.getPrice();
                }
                totalStock += skuItem.getStock();
            }

            spu.setMinPrice(minPrice);
            spu.setMaxPrice(maxPrice);
            spu.setTotalStock(totalStock);
        }

        productSpuMapper.updateById(spu);
    }

    /**
     * Publish SPU (set status to ON_SHELF).
     */
    @Transactional(rollbackFor = Exception.class)
    public void publishSpu(Long spuId, Long tenantId) {
        ProductSpu spu = productSpuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (!spu.getTenantId().equals(tenantId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (spu.getStatus() == ProductStatusEnum.ON_SHELF.getCode()) {
            throw new BusinessException("商品已上架");
        }

        spu.setStatus(ProductStatusEnum.ON_SHELF.getCode());
        productSpuMapper.updateById(spu);
    }

    /**
     * Unpublish SPU (set status to OFF_SHELF).
     */
    @Transactional(rollbackFor = Exception.class)
    public void unpublishSpu(Long spuId, Long tenantId) {
        ProductSpu spu = productSpuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (!spu.getTenantId().equals(tenantId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (spu.getStatus() != ProductStatusEnum.ON_SHELF.getCode()) {
            throw new BusinessException("商品未上架，无法下架");
        }

        spu.setStatus(ProductStatusEnum.OFF_SHELF.getCode());
        productSpuMapper.updateById(spu);
    }

    /**
     * Get SPU list (paginated + filtered).
     */
    public PageResult<SpuVO> getSpuList(SpuQueryDTO dto, Long tenantId) {
        Page<ProductSpu> pageParam = new Page<>(dto.getPage(), dto.getLimit());
        LambdaQueryWrapper<ProductSpu> wrapper = new LambdaQueryWrapper<>();

        if (tenantId != null) {
            wrapper.eq(ProductSpu::getTenantId, tenantId);
        }
        if (dto.getCategoryId() != null) {
            wrapper.eq(ProductSpu::getCategoryId, dto.getCategoryId());
        }
        if (dto.getBrandId() != null) {
            wrapper.eq(ProductSpu::getBrandId, dto.getBrandId());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(ProductSpu::getStatus, dto.getStatus());
        }
        if (dto.getKeyword() != null && !dto.getKeyword().isEmpty()) {
            wrapper.like(ProductSpu::getProductName, dto.getKeyword());
        }
        if (dto.getMinPrice() != null) {
            wrapper.ge(ProductSpu::getMinPrice, dto.getMinPrice());
        }
        if (dto.getMaxPrice() != null) {
            wrapper.le(ProductSpu::getMaxPrice, dto.getMaxPrice());
        }

        // Sorting
        if ("price".equals(dto.getSortField())) {
            wrapper.orderBy(true, "asc".equals(dto.getSortOrder()), ProductSpu::getMinPrice);
        } else if ("sales".equals(dto.getSortField())) {
            wrapper.orderBy(true, "asc".equals(dto.getSortOrder()), ProductSpu::getTotalSales);
        } else {
            wrapper.orderByDesc(ProductSpu::getCreateTime);
        }

        Page<ProductSpu> result = productSpuMapper.selectPage(pageParam, wrapper);

        List<SpuVO> voList = result.getRecords().stream()
                .map(this::toSpuVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), dto.getPage(), dto.getLimit());
    }

    /**
     * Get SPU detail (SPU + SKU + images + attributes).
     */
    public SpuDetailVO getSpuDetail(Long spuId) {
        ProductSpu spu = productSpuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        SpuDetailVO vo = new SpuDetailVO();
        vo.setId(spu.getId());
        vo.setCategoryId(spu.getCategoryId());
        vo.setBrandId(spu.getBrandId());
        vo.setProductName(spu.getProductName());
        vo.setSubTitle(spu.getSubTitle());
        vo.setMainImage(spu.getMainImage());
        vo.setImages(spu.getImages() != null
                ? Arrays.asList(spu.getImages().split(",")) : new ArrayList<>());
        vo.setDescription(spu.getDescription());
        vo.setMinPrice(spu.getMinPrice());
        vo.setMaxPrice(spu.getMaxPrice());
        vo.setTotalStock(spu.getTotalStock());
        vo.setTotalSales(spu.getTotalSales());
        vo.setStatus(spu.getStatus());
        vo.setTenantId(spu.getTenantId());
        vo.setCreateTime(spu.getCreateTime());

        // Fill tenant/category/brand names
        if (spu.getTenantId() != null && spu.getTenantId() > 0) {
            Tenant tenant = tenantMapper.selectById(spu.getTenantId());
            if (tenant != null) {
                vo.setTenantName(tenant.getTenantName());
            }
        }
        if (spu.getCategoryId() != null) {
            ProductCategory category = productCategoryMapper.selectById(spu.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        }
        if (spu.getBrandId() != null) {
            ProductBrand brand = productBrandMapper.selectById(spu.getBrandId());
            if (brand != null) {
                vo.setBrandName(brand.getBrandName());
            }
        }

        // SKU list
        List<ProductSku> skuList = productSkuMapper.selectList(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getSpuId, spuId)
                        .eq(ProductSku::getStatus, 1));
        List<SpuDetailVO.SkuVO> skuVOList = skuList.stream().map(sku -> {
            SpuDetailVO.SkuVO skuVO = new SpuDetailVO.SkuVO();
            skuVO.setId(sku.getId());
            skuVO.setSkuName(sku.getSkuName());
            skuVO.setSkuCode(sku.getSkuCode());
            skuVO.setSpecValues(sku.getSpecValues());
            skuVO.setPrice(sku.getPrice());
            skuVO.setOriginalPrice(sku.getOriginalPrice());
            skuVO.setStock(sku.getStock());
            skuVO.setImage(sku.getImage());
            skuVO.setWeight(sku.getWeight());
            skuVO.setStatus(sku.getStatus());
            return skuVO;
        }).collect(Collectors.toList());
        vo.setSkuList(skuVOList);

        // Attribute values
        List<ProductAttributeValue> attrList = productAttributeValueMapper.selectList(
                new LambdaQueryWrapper<ProductAttributeValue>()
                        .eq(ProductAttributeValue::getSpuId, spuId));
        List<SpuDetailVO.AttributeValueVO> attrVOList = attrList.stream().map(attr -> {
            SpuDetailVO.AttributeValueVO attrVO = new SpuDetailVO.AttributeValueVO();
            attrVO.setAttributeId(attr.getAttributeId());
            attrVO.setAttributeName(attr.getAttributeName());
            attrVO.setAttributeValue(attr.getAttributeValue());
            attrVO.setAttributeType(attr.getAttributeType());
            return attrVO;
        }).collect(Collectors.toList());
        vo.setAttributeList(attrVOList);

        // Images
        List<ProductImage> imageList = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getSpuId, spuId)
                        .orderByAsc(ProductImage::getSortOrder));
        List<SpuDetailVO.ProductImageVO> imageVOList = imageList.stream().map(img -> {
            SpuDetailVO.ProductImageVO imgVO = new SpuDetailVO.ProductImageVO();
            imgVO.setId(img.getId());
            imgVO.setImageUrl(img.getImageUrl());
            imgVO.setSortOrder(img.getSortOrder());
            imgVO.setImageType(img.getImageType());
            return imgVO;
        }).collect(Collectors.toList());
        vo.setImageList(imageVOList);

        return vo;
    }

    /**
     * Audit SPU.
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditSpu(Long spuId, boolean pass, String reason) {
        ProductSpu spu = productSpuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        spu.setAuditStatus(pass ? 1 : 2);
        spu.setAuditRemark(reason);
        productSpuMapper.updateById(spu);
    }

    /**
     * Submit a product comment (review) from a member.
     * Validates that the member has a completed order containing this item.
     */
    @Transactional(rollbackFor = Exception.class)
    public Long submitComment(Long memberId, CommentCreateDTO dto) {
        // Validate the order item exists
        OrderItem orderItem = orderItemMapper.selectById(dto.getOrderItemId());
        if (orderItem == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        // Validate the order belongs to this member and is completed
        OrderMain order = orderMainMapper.selectById(orderItem.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (order.getOrderStatus() != OrderStatusEnum.COMPLETED.getCode()) {
            throw new BusinessException("只能对已完成的订单进行评价");
        }

        // Validate SPU matches the order item
        if (!orderItem.getSpuId().equals(dto.getSpuId())) {
            throw new BusinessException("商品与订单项不匹配");
        }

        // Check for duplicate comment on this order item
        Long existingCount = productCommentMapper.selectCount(
                new LambdaQueryWrapper<ProductComment>()
                        .eq(ProductComment::getOrderItemId, dto.getOrderItemId())
                        .eq(ProductComment::getMemberId, memberId));
        if (existingCount > 0) {
            throw new BusinessException("该订单项已评价，不能重复评价");
        }

        ProductComment comment = new ProductComment();
        comment.setOrderItemId(dto.getOrderItemId());
        comment.setSpuId(dto.getSpuId());
        comment.setMemberId(memberId);
        comment.setContent(dto.getContent());
        comment.setImages(dto.getImages() != null ? String.join(",", dto.getImages()) : null);
        comment.setScore(dto.getScore());
        comment.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : 0);
        comment.setStatus(1); // Default to visible
        comment.setTenantId(orderItem.getTenantId());

        productCommentMapper.insert(comment);
        return comment.getId();
    }

    private SpuVO toSpuVO(ProductSpu spu) {
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

        // Fill names
        if (spu.getTenantId() != null && spu.getTenantId() > 0) {
            Tenant tenant = tenantMapper.selectById(spu.getTenantId());
            if (tenant != null) {
                vo.setTenantName(tenant.getTenantName());
            }
        }
        if (spu.getCategoryId() != null) {
            ProductCategory category = productCategoryMapper.selectById(spu.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        }
        if (spu.getBrandId() != null) {
            ProductBrand brand = productBrandMapper.selectById(spu.getBrandId());
            if (brand != null) {
                vo.setBrandName(brand.getBrandName());
            }
        }

        return vo;
    }
}
