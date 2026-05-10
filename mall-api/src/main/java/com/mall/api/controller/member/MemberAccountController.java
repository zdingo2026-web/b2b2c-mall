package com.mall.api.controller.member;

import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.dto.member.RealnameAuthDTO;
import com.mall.model.vo.member.MemberCouponVO;
import com.mall.model.vo.member.MemberLevelVO;
import com.mall.model.vo.member.MemberRedPacketVO;
import com.mall.model.vo.member.RealnameAuthVO;
import com.mall.model.vo.promotion.CouponOptionVO;
import com.mall.service.promotion.CouponTemplateService;
import com.mall.service.member.MemberLevelService;
import com.mall.service.member.MemberRealnameAuthService;
import com.mall.service.member.RedPacketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/member/account")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class MemberAccountController {

    private final MemberRealnameAuthService memberRealnameAuthService;
    private final RedPacketService redPacketService;
    private final MemberLevelService memberLevelService;
    private final CouponTemplateService couponTemplateService;

    @PostMapping("/realname")
    public R<Void> submitRealname(@Validated @RequestBody RealnameAuthDTO dto) {
        Long memberId = UserContext.getUserId();
        memberRealnameAuthService.submitAuth(memberId, dto);
        return R.ok();
    }

    @GetMapping("/realname/status")
    public R<RealnameAuthVO> realnameStatus() {
        Long memberId = UserContext.getUserId();
        return R.ok(memberRealnameAuthService.getAuthStatus(memberId));
    }

    @GetMapping("/red-packet/list")
    public R<PageResult<MemberRedPacketVO>> redPacketList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long memberId = UserContext.getUserId();
        return R.ok(redPacketService.memberRedPackets(memberId, status, page, limit));
    }

    @PostMapping("/red-packet/{batchId}/claim")
    public R<MemberRedPacketVO> claimRedPacket(@PathVariable Long batchId) {
        Long memberId = UserContext.getUserId();
        return R.ok(redPacketService.claimRedPacket(memberId, batchId));
    }

    @GetMapping("/level/list")
    public R<List<MemberLevelVO>> levelList() {
        return R.ok(memberLevelService.list());
    }

    @GetMapping("/coupon/list")
    public R<PageResult<MemberCouponVO>> couponList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long memberId = UserContext.getUserId();
        return R.ok(couponTemplateService.memberCoupons(memberId, status, page, limit));
    }

    @PostMapping("/coupon/{templateId}/claim")
    public R<Void> claimCoupon(@PathVariable Long templateId) {
        Long memberId = UserContext.getUserId();
        couponTemplateService.claimCoupon(memberId, templateId);
        return R.ok();
    }

    @GetMapping("/coupon/available")
    public R<List<CouponOptionVO>> availableCoupons(
            @RequestParam Long spuId,
            @RequestParam BigDecimal orderAmount) {
        Long memberId = UserContext.getUserId();
        return R.ok(couponTemplateService.availableCoupons(memberId, spuId, orderAmount));
    }
}
