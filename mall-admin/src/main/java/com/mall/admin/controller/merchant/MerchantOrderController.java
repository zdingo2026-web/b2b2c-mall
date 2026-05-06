package com.mall.admin.controller.merchant;

import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.dto.OrderQueryDTO;
import com.mall.model.vo.OrderMainVO;
import com.mall.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Merchant order management controller.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/merchant/order")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantOrderController {

    private final OrderService orderService;

    @GetMapping("/list")
    public R<PageResult<OrderMainVO>> list(OrderQueryDTO dto) {
        Long tenantId = UserContext.getTenantId();
        return R.ok(orderService.getOrderListByTenant(tenantId, dto));
    }

    @GetMapping("/{orderNo}")
    public R<OrderMainVO> detail(@PathVariable String orderNo) {
        return R.ok(orderService.getOrderDetail(orderNo));
    }

    @PutMapping("/item/{id}/ship")
    public R<Void> ship(@PathVariable Long id,
                         @RequestParam String logisticsCompany,
                         @RequestParam String logisticsNo) {
        orderService.shipOrder(id, logisticsCompany, logisticsNo);
        return R.ok();
    }

    @PutMapping("/{orderNo}/cancel")
    public R<Void> cancel(@PathVariable String orderNo) {
        orderService.cancelOrderByMerchant(orderNo);
        return R.ok();
    }
}
