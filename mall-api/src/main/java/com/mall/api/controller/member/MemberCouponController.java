package com.mall.api.controller.member;

import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.entity.MemberCoupon;
import com.mall.service.user.MemberCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Member coupon controller (C-end).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/member/coupon")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class MemberCouponController {

    private final MemberCouponService memberCouponService;

    @GetMapping("/list")
    public R<PageResult<MemberCoupon>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long memberId = UserContext.getUserId();
        return R.ok(memberCouponService.getCouponList(memberId, status, page, limit));
    }
}
