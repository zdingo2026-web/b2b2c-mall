package com.mall.api.controller.member;

import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.dto.OrderCreateDTO;
import com.mall.model.dto.RefundApplyDTO;
import com.mall.model.entity.OrderRefund;
import com.mall.model.vo.OrderMainVO;
import com.mall.model.vo.OrderLogisticsVO;
import com.mall.service.order.OrderRefundService;
import com.mall.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Member order controller (C-end).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/member/order")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class MemberOrderController {

    private final OrderService orderService;
    private final OrderRefundService orderRefundService;

    @PostMapping("/create")
    public R<OrderMainVO> create(@Validated @RequestBody OrderCreateDTO dto) {
        Long memberId = UserContext.getUserId();
        return R.ok(orderService.createOrder(memberId, dto));
    }

    @GetMapping("/list")
    public R<PageResult<OrderMainVO>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long memberId = UserContext.getUserId();
        return R.ok(orderService.getOrderList(memberId, status, page, limit));
    }

    @GetMapping("/{orderNo}")
    public R<OrderMainVO> detail(@PathVariable String orderNo) {
        return R.ok(orderService.getOrderDetail(orderNo));
    }

    @PutMapping("/{orderNo}/cancel")
    public R<Void> cancel(@PathVariable String orderNo) {
        Long memberId = UserContext.getUserId();
        orderService.cancelOrder(memberId, orderNo);
        return R.ok();
    }

    @PutMapping("/item/{id}/confirm")
    public R<Void> confirm(@PathVariable Long id) {
        Long memberId = UserContext.getUserId();
        orderService.confirmReceive(memberId, id);
        return R.ok();
    }

    @PostMapping("/refund/apply")
    public R<Long> refundApply(@Validated @RequestBody RefundApplyDTO dto) {
        Long memberId = UserContext.getUserId();
        return R.ok(orderRefundService.applyRefund(memberId, dto));
    }

    /**
     * 查询物流信息
     */
    @GetMapping("/{orderNo}/logistics")
    public R<OrderLogisticsVO> logistics(@PathVariable String orderNo) {
        return R.ok(orderService.getLogistics(orderNo));
    }

    // ==================== C-12: Refund Progress Query ====================

    /**
     * Query refund list for the current member (paginated).
     */
    @GetMapping("/refund/list")
    public R<PageResult<OrderRefund>> refundList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long memberId = UserContext.getUserId();
        return R.ok(orderRefundService.getRefundListByMember(memberId, page, limit));
    }

    /**
     * Query refund detail for the current member.
     */
    @GetMapping("/refund/{id}")
    public R<OrderRefund> refundDetail(@PathVariable Long id) {
        Long memberId = UserContext.getUserId();
        return R.ok(orderRefundService.getRefundDetailByMember(memberId, id));
    }
}
