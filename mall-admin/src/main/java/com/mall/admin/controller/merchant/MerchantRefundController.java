package com.mall.admin.controller.merchant;

import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.entity.OrderRefund;
import com.mall.service.order.OrderRefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Merchant refund management controller.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/merchant/order/refund")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantRefundController {

    private final OrderRefundService orderRefundService;

    @GetMapping("/list")
    public R<PageResult<OrderRefund>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long tenantId = UserContext.getTenantId();
        return R.ok(orderRefundService.getRefundListByTenant(tenantId, page, limit));
    }

    @PutMapping("/{id}/handle")
    public R<Void> handle(@PathVariable Long id,
                           @RequestParam Boolean pass,
                           @RequestParam(required = false) String reason) {
        orderRefundService.handleRefund(id, pass, reason);
        return R.ok();
    }
}
