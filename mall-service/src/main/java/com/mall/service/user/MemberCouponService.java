package com.mall.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.response.PageResult;
import com.mall.dao.mapper.MemberCouponMapper;
import com.mall.model.entity.MemberCoupon;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Member coupon service.
 * Handles coupon listing and count for a member.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberCouponService {

    private final MemberCouponMapper memberCouponMapper;

    /**
     * Get paginated coupon list for a member.
     *
     * @param memberId member ID
     * @param status   coupon status filter (null for all)
     * @param page     page number
     * @param limit    page size
     * @return paginated coupon list
     */
    public PageResult<MemberCoupon> getCouponList(Long memberId, Integer status, int page, int limit) {
        Page<MemberCoupon> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<MemberCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberCoupon::getMemberId, memberId);
        if (status != null) {
            wrapper.eq(MemberCoupon::getStatus, status);
        }
        wrapper.orderByDesc(MemberCoupon::getCreateTime);

        Page<MemberCoupon> result = memberCouponMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, limit);
    }

    /**
     * Count usable coupons for a member.
     *
     * @param memberId member ID
     * @return usable coupon count
     */
    public long getCouponCount(Long memberId) {
        return memberCouponMapper.selectCount(
                new LambdaQueryWrapper<MemberCoupon>()
                        .eq(MemberCoupon::getMemberId, memberId)
                        .eq(MemberCoupon::getStatus, 0));
    }
}
