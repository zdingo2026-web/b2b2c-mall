package com.mall.admin.controller.platform;

import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.model.dto.OrderQueryDTO;
import com.mall.model.vo.OrderMainVO;
import com.mall.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Platform order management controller.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/platform/order")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM')")
public class PlatformOrderController {

    private final OrderService orderService;

    @GetMapping("/list")
    public R<PageResult<OrderMainVO>> list(OrderQueryDTO dto) {
        return R.ok(orderService.getOrderList(null, dto.getOrderStatus(), dto.getPage(), dto.getLimit()));
    }

    @GetMapping("/{orderNo}")
    public R<OrderMainVO> detail(@PathVariable String orderNo) {
        return R.ok(orderService.getOrderDetail(orderNo));
    }

    @PutMapping("/{orderNo}/ship")
    public R<Void> ship(@PathVariable String orderNo,
                         @RequestParam String logisticsCompany,
                         @RequestParam String logisticsNo) {
        orderService.shipOrderByOrderNo(orderNo, logisticsCompany, logisticsNo);
        return R.ok();
    }

    @PutMapping("/{orderNo}/cancel")
    public R<Void> cancel(@PathVariable String orderNo) {
        orderService.cancelOrderByPlatform(orderNo);
        return R.ok();
    }
}
