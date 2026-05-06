package com.mall.admin.controller.merchant;

import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.vo.MerchantOverviewVO;
import com.mall.model.vo.ProductRankVO;
import com.mall.model.vo.TrendDataVO;
import com.mall.service.system.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Merchant statistics controller.
 */
@RestController
@RequestMapping("/api/v1/merchant/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantStatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/overview")
    public R<MerchantOverviewVO> getMerchantOverview() {
        Long tenantId = UserContext.getTenantId();
        return R.ok(statisticsService.getMerchantOverview(tenantId));
    }

    @GetMapping("/trend")
    public R<List<TrendDataVO>> getTrendData(
            @RequestParam(defaultValue = "7") Integer days) {
        Long tenantId = UserContext.getTenantId();
        return R.ok(statisticsService.getMerchantTrend(tenantId, days));
    }

    @GetMapping("/product-ranking")
    public R<List<ProductRankVO>> getProductRanking() {
        Long tenantId = UserContext.getTenantId();
        return R.ok(statisticsService.getMerchantProductRanking(tenantId));
    }
}
