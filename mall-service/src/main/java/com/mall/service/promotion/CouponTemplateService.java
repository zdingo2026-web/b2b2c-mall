package com.mall.service.promotion;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.MemberCouponMapper;
import com.mall.dao.mapper.promotion.CouponTemplateMapper;
import com.mall.model.dto.promotion.CouponTemplateCreateDTO;
import com.mall.model.entity.MemberCoupon;
import com.mall.model.entity.promotion.CouponTemplate;
import com.mall.model.vo.member.MemberCouponVO;
import com.mall.model.vo.promotion.CouponOptionVO;
import com.mall.model.vo.promotion.CouponTemplateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponTemplateService {

    private final CouponTemplateMapper couponTemplateMapper;
    private final MemberCouponMapper memberCouponMapper;

    public PageResult<CouponTemplateVO> list(Long tenantId, Integer couponType, Integer status, int page, int limit) {
        Page<CouponTemplate> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<CouponTemplate> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(CouponTemplate::getTenantId, tenantId);
        }
        if (couponType != null) {
            wrapper.eq(CouponTemplate::getCouponType, couponType);
        }
        if (status != null) {
            wrapper.eq(CouponTemplate::getStatus, status);
        }
        wrapper.orderByDesc(CouponTemplate::getCreateTime);
        Page<CouponTemplate> result = couponTemplateMapper.selectPage(pageParam, wrapper);
        List<CouponTemplateVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    public CouponTemplateVO detail(Long id) {
        CouponTemplate template = couponTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return toVO(template);
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(Long tenantId, CouponTemplateCreateDTO dto) {
        CouponTemplate template = new CouponTemplate();
        BeanUtils.copyProperties(dto, template);
        template.setTenantId(tenantId);
        template.setRemainCount(dto.getTotalCount());
        template.setIssuerType(tenantId != null && tenantId > 0 ? 2 : 1);
        template.setCanStackSeckill(dto.getCanStackSeckill() != null && dto.getCanStackSeckill() ? 1 : 0);
        template.setStatus(1);
        couponTemplateMapper.insert(template);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CouponTemplateCreateDTO dto) {
        CouponTemplate template = couponTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (dto.getCouponName() != null) {
            template.setCouponName(dto.getCouponName());
        }
        if (dto.getCouponType() != null) {
            template.setCouponType(dto.getCouponType());
        }
        if (dto.getCouponValue() != null) {
            template.setCouponValue(dto.getCouponValue());
        }
        if (dto.getMinAmount() != null) {
            template.setMinAmount(dto.getMinAmount());
        }
        if (dto.getMaxDiscount() != null) {
            template.setMaxDiscount(dto.getMaxDiscount());
        }
        if (dto.getLimitPerUser() != null) {
            template.setLimitPerUser(dto.getLimitPerUser());
        }
        if (dto.getApplyScope() != null) {
            template.setApplyScope(dto.getApplyScope());
        }
        if (dto.getApplyCategoryIds() != null) {
            template.setApplyCategoryIds(dto.getApplyCategoryIds());
        }
        if (dto.getApplySpuIds() != null) {
            template.setApplySpuIds(dto.getApplySpuIds());
        }
        if (dto.getValidType() != null) {
            template.setValidType(dto.getValidType());
        }
        if (dto.getValidStartTime() != null) {
            template.setValidStartTime(dto.getValidStartTime());
        }
        if (dto.getValidEndTime() != null) {
            template.setValidEndTime(dto.getValidEndTime());
        }
        if (dto.getValidDays() != null) {
            template.setValidDays(dto.getValidDays());
        }
        if (dto.getCanStackSeckill() != null) {
            template.setCanStackSeckill(dto.getCanStackSeckill() ? 1 : 0);
        }
        couponTemplateMapper.updateById(template);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        CouponTemplate template = couponTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        couponTemplateMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void claimCoupon(Long memberId, Long templateId) {
        CouponTemplate template = couponTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (template.getStatus() != 1) {
            throw new BusinessException("优惠券未上架");
        }
        if (template.getRemainCount() <= 0) {
            throw new BusinessException("优惠券已领完");
        }
        Long claimedCount = memberCouponMapper.selectCount(
                new LambdaQueryWrapper<MemberCoupon>()
                        .eq(MemberCoupon::getMemberId, memberId)
                        .eq(MemberCoupon::getCouponTemplateId, templateId));
        if (claimedCount >= template.getLimitPerUser()) {
            throw new BusinessException("已达到领取上限");
        }
        template.setRemainCount(template.getRemainCount() - 1);
        couponTemplateMapper.updateById(template);

        MemberCoupon memberCoupon = new MemberCoupon();
        memberCoupon.setMemberId(memberId);
        memberCoupon.setCouponName(template.getCouponName());
        memberCoupon.setCouponType(template.getCouponType());
        memberCoupon.setCouponValue(template.getCouponValue());
        memberCoupon.setMinAmount(template.getMinAmount());
        memberCoupon.setStatus(0);
        memberCoupon.setCouponTemplateId(templateId);
        memberCoupon.setSourceType(1);
        memberCoupon.setSourceId(templateId);
        memberCoupon.setTenantId(template.getTenantId());
        memberCoupon.setCanStackSeckill(template.getCanStackSeckill());

        if (template.getValidType() == 1) {
            memberCoupon.setValidStartTime(template.getValidStartTime());
            memberCoupon.setExpireTime(template.getValidEndTime());
        } else if (template.getValidType() == 2) {
            memberCoupon.setValidStartTime(LocalDateTime.now());
            memberCoupon.setExpireTime(LocalDateTime.now().plusDays(template.getValidDays()));
        }
        memberCouponMapper.insert(memberCoupon);
    }

    public PageResult<MemberCouponVO> memberCoupons(Long memberId, Integer status, int page, int limit) {
        Page<MemberCoupon> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<MemberCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberCoupon::getMemberId, memberId);
        if (status != null) {
            wrapper.eq(MemberCoupon::getStatus, status);
        }
        wrapper.orderByDesc(MemberCoupon::getCreateTime);
        Page<MemberCoupon> result = memberCouponMapper.selectPage(pageParam, wrapper);
        List<MemberCouponVO> voList = result.getRecords().stream().map(this::toMemberCouponVO).collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    public List<CouponOptionVO> availableCoupons(Long memberId, Long spuId, BigDecimal orderAmount) {
        List<MemberCoupon> coupons = memberCouponMapper.selectList(
                new LambdaQueryWrapper<MemberCoupon>()
                        .eq(MemberCoupon::getMemberId, memberId)
                        .eq(MemberCoupon::getStatus, 0)
                        .le(MemberCoupon::getValidStartTime, LocalDateTime.now())
                        .ge(MemberCoupon::getExpireTime, LocalDateTime.now()));
        List<CouponOptionVO> result = new ArrayList<>();
        for (MemberCoupon coupon : coupons) {
            CouponOptionVO option = new CouponOptionVO();
            option.setMemberCouponId(coupon.getId());
            option.setCouponName(coupon.getCouponName());
            option.setCouponType(coupon.getCouponType());
            option.setCouponValue(coupon.getCouponValue());
            option.setMinAmount(coupon.getMinAmount());
            option.setApplicable(true);
            option.setRejectReason(null);
            if (coupon.getMinAmount() != null && orderAmount.compareTo(coupon.getMinAmount()) < 0) {
                option.setApplicable(false);
                option.setRejectReason("未满足最低消费金额");
            }
            if (option.getApplicable()) {
                option.setDiscountAmount(calculateDiscount(coupon, orderAmount));
            }
            result.add(option);
        }
        return result;
    }

    private BigDecimal calculateDiscount(MemberCoupon coupon, BigDecimal orderAmount) {
        if (coupon.getCouponType() == 1) {
            return coupon.getCouponValue();
        } else if (coupon.getCouponType() == 2) {
            BigDecimal discount = orderAmount.multiply(BigDecimal.ONE.subtract(coupon.getCouponValue().divide(BigDecimal.TEN)));
            return discount;
        } else {
            return coupon.getCouponValue();
        }
    }

    private CouponTemplateVO toVO(CouponTemplate template) {
        CouponTemplateVO vo = new CouponTemplateVO();
        vo.setId(template.getId());
        vo.setTenantId(template.getTenantId());
        vo.setCouponName(template.getCouponName());
        vo.setCouponType(template.getCouponType());
        vo.setCouponValue(template.getCouponValue());
        vo.setMinAmount(template.getMinAmount());
        vo.setMaxDiscount(template.getMaxDiscount());
        vo.setTotalCount(template.getTotalCount());
        vo.setRemainCount(template.getRemainCount());
        vo.setLimitPerUser(template.getLimitPerUser());
        vo.setIssuerType(template.getIssuerType());
        vo.setApplyScope(template.getApplyScope());
        vo.setValidType(template.getValidType());
        vo.setValidStartTime(template.getValidStartTime());
        vo.setValidEndTime(template.getValidEndTime());
        vo.setValidDays(template.getValidDays());
        vo.setCanStackSeckill(template.getCanStackSeckill() != null && template.getCanStackSeckill() == 1);
        vo.setStatus(template.getStatus());
        vo.setCreateTime(template.getCreateTime());
        return vo;
    }

    private MemberCouponVO toMemberCouponVO(MemberCoupon coupon) {
        MemberCouponVO vo = new MemberCouponVO();
        vo.setId(coupon.getId());
        vo.setCouponTemplateId(coupon.getCouponTemplateId());
        vo.setTenantId(coupon.getTenantId());
        vo.setCouponName(coupon.getCouponName());
        vo.setCouponType(coupon.getCouponType());
        vo.setCouponValue(coupon.getCouponValue());
        vo.setMinAmount(coupon.getMinAmount());
        vo.setSourceType(coupon.getSourceType());
        vo.setStatus(coupon.getStatus());
        vo.setValidStartTime(coupon.getValidStartTime());
        vo.setExpireTime(coupon.getExpireTime());
        vo.setUseTime(coupon.getUseTime());
        return vo;
    }
}
