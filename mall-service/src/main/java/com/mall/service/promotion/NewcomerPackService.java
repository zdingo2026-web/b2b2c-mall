package com.mall.service.promotion;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.MemberCouponMapper;
import com.mall.dao.mapper.promotion.CouponTemplateMapper;
import com.mall.dao.mapper.promotion.NewcomerPackMapper;
import com.mall.model.dto.promotion.NewcomerPackDTO;
import com.mall.model.entity.MemberCoupon;
import com.mall.model.entity.promotion.CouponTemplate;
import com.mall.model.entity.promotion.NewcomerPack;
import com.mall.model.vo.promotion.NewcomerPackVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewcomerPackService {

    private final NewcomerPackMapper newcomerPackMapper;
    private final CouponTemplateMapper couponTemplateMapper;
    private final MemberCouponMapper memberCouponMapper;

    public NewcomerPackVO getConfig() {
        List<NewcomerPack> packs = newcomerPackMapper.selectList(null);
        if (packs.isEmpty()) {
            NewcomerPackVO vo = new NewcomerPackVO();
            vo.setEnabled(false);
            return vo;
        }
        NewcomerPack pack = packs.get(0);
        return toVO(pack);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(NewcomerPackDTO dto) {
        List<NewcomerPack> packs = newcomerPackMapper.selectList(null);
        NewcomerPack pack;
        if (packs.isEmpty()) {
            pack = new NewcomerPack();
            pack.setPackName(dto.getPackName());
            pack.setPackDesc(dto.getPackDesc());
            pack.setEnabled(dto.getEnabled() != null && dto.getEnabled() ? 1 : 0);
            pack.setCouponIds(dto.getCouponIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
            newcomerPackMapper.insert(pack);
        } else {
            pack = packs.get(0);
            if (dto.getPackName() != null) {
                pack.setPackName(dto.getPackName());
            }
            if (dto.getPackDesc() != null) {
                pack.setPackDesc(dto.getPackDesc());
            }
            if (dto.getEnabled() != null) {
                pack.setEnabled(dto.getEnabled() ? 1 : 0);
            }
            if (dto.getCouponIds() != null) {
                pack.setCouponIds(dto.getCouponIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
            }
            newcomerPackMapper.updateById(pack);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void claimPack(Long memberId) {
        List<NewcomerPack> packs = newcomerPackMapper.selectList(null);
        if (packs.isEmpty()) {
            throw new BusinessException("新人礼包未配置");
        }
        NewcomerPack pack = packs.get(0);
        if (pack.getEnabled() == null || pack.getEnabled() != 1) {
            throw new BusinessException("新人礼包未启用");
        }

        Long claimedCount = memberCouponMapper.selectCount(
                new LambdaQueryWrapper<MemberCoupon>()
                        .eq(MemberCoupon::getMemberId, memberId)
                        .eq(MemberCoupon::getSourceType, 3));
        if (claimedCount > 0) {
            throw new BusinessException("已领取过新人礼包");
        }

        if (pack.getCouponIds() == null || pack.getCouponIds().isEmpty()) {
            throw new BusinessException("礼包中无优惠券");
        }
        List<Long> couponIds = Arrays.stream(pack.getCouponIds().split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        for (Long couponId : couponIds) {
            CouponTemplate template = couponTemplateMapper.selectById(couponId);
            if (template == null || template.getStatus() != 1) {
                continue;
            }
            MemberCoupon memberCoupon = new MemberCoupon();
            memberCoupon.setMemberId(memberId);
            memberCoupon.setCouponName(template.getCouponName());
            memberCoupon.setCouponType(template.getCouponType());
            memberCoupon.setCouponValue(template.getCouponValue());
            memberCoupon.setMinAmount(template.getMinAmount());
            memberCoupon.setStatus(0);
            memberCoupon.setCouponTemplateId(template.getId());
            memberCoupon.setSourceType(3);
            memberCoupon.setSourceId(pack.getId());
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
    }

    private NewcomerPackVO toVO(NewcomerPack pack) {
        NewcomerPackVO vo = new NewcomerPackVO();
        vo.setId(pack.getId());
        vo.setPackName(pack.getPackName());
        vo.setPackDesc(pack.getPackDesc());
        vo.setEnabled(pack.getEnabled() != null && pack.getEnabled() == 1);
        if (pack.getCouponIds() != null && !pack.getCouponIds().isEmpty()) {
            vo.setCouponIds(Arrays.stream(pack.getCouponIds().split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList()));
        }
        return vo;
    }
}
