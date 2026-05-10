package com.mall.api.controller.member;

import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.dto.points.PointsOrderCreateDTO;
import com.mall.model.vo.points.*;
import com.mall.service.points.PointsAccountService;
import com.mall.service.points.PointsCheckinService;
import com.mall.service.points.PointsOrderService;
import com.mall.service.points.PointsProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/member/points")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class MemberPointsController {

    private final PointsAccountService pointsAccountService;
    private final PointsProductService pointsProductService;
    private final PointsOrderService pointsOrderService;
    private final PointsCheckinService pointsCheckinService;

    @GetMapping("/account")
    public R<PointsAccountVO> getAccount() {
        Long memberId = UserContext.getUserId();
        return R.ok(pointsAccountService.getAccount(memberId));
    }

    @GetMapping("/details")
    public R<PageResult<PointsDetailVO>> details(
            @RequestParam(required = false) Integer bizType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long memberId = UserContext.getUserId();
        return R.ok(pointsAccountService.details(memberId, bizType, page, limit));
    }

    @GetMapping("/product/list")
    public R<PageResult<PointsProductVO>> productList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(pointsProductService.list(categoryId, status, page, limit));
    }

    @GetMapping("/product/{id}")
    public R<PointsProductDetailVO> productDetail(@PathVariable Long id) {
        return R.ok(pointsProductService.detail(id));
    }

    @PostMapping("/order/create")
    public R<PointsOrderVO> createOrder(@Validated @RequestBody PointsOrderCreateDTO dto) {
        Long memberId = UserContext.getUserId();
        return R.ok(pointsOrderService.createOrder(memberId, dto));
    }

    @GetMapping("/order/list")
    public R<PageResult<PointsOrderVO>> orderList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long memberId = UserContext.getUserId();
        return R.ok(pointsOrderService.orderList(memberId, status, page, limit));
    }

    @GetMapping("/order/{id}")
    public R<PointsOrderVO> orderDetail(@PathVariable Long id) {
        Long memberId = UserContext.getUserId();
        return R.ok(pointsOrderService.orderDetail(id, memberId));
    }

    @PutMapping("/order/{id}/cancel")
    public R<Void> cancelOrder(@PathVariable Long id) {
        Long memberId = UserContext.getUserId();
        pointsOrderService.cancelOrder(id, memberId);
        return R.ok();
    }

    @PutMapping("/order/{id}/receive")
    public R<Void> receiveOrder(@PathVariable Long id) {
        Long memberId = UserContext.getUserId();
        pointsOrderService.receiveOrder(id, memberId);
        return R.ok();
    }

    @PostMapping("/checkin")
    public R<CheckinResultVO> checkin() {
        Long memberId = UserContext.getUserId();
        return R.ok(pointsCheckinService.checkin(memberId));
    }

    @GetMapping("/checkin/status")
    public R<CheckinStatusVO> checkinStatus() {
        Long memberId = UserContext.getUserId();
        return R.ok(pointsCheckinService.checkinStatus(memberId));
    }
}
