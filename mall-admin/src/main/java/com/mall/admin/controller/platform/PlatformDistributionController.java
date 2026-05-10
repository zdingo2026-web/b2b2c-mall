package com.mall.admin.controller.platform;

import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.dto.distribution.DistributionConfigDTO;
import com.mall.model.vo.distribution.DistributionConfigVO;
import com.mall.model.vo.distribution.DistributorRankVO;
import com.mall.model.vo.distribution.DistributorVO;
import com.mall.model.vo.distribution.WithdrawRecordVO;
import com.mall.service.distribution.CommissionService;
import com.mall.service.distribution.DistributionConfigService;
import com.mall.service.distribution.DistributorService;
import com.mall.service.distribution.WithdrawService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/platform/distribution")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM')")
public class PlatformDistributionController {

    private final DistributionConfigService distributionConfigService;
    private final DistributorService distributorService;
    private final WithdrawService withdrawService;
    private final CommissionService commissionService;

    @GetMapping("/config")
    public R<DistributionConfigVO> getConfig() {
        return R.ok(distributionConfigService.getConfig());
    }

    @PutMapping("/config")
    public R<Void> saveConfig(@Validated @RequestBody DistributionConfigDTO dto) {
        distributionConfigService.saveConfig(dto);
        return R.ok();
    }

    @GetMapping("/distributor/list")
    public R<PageResult<DistributorVO>> distributorList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(distributorService.list(keyword, status, page, limit));
    }

    @PutMapping("/distributor/{id}/audit")
    public R<Void> auditDistributor(@PathVariable Long id,
                                     @RequestParam Integer status,
                                     @RequestParam(required = false) String rejectReason) {
        distributorService.audit(id, status, rejectReason);
        return R.ok();
    }

    @GetMapping("/withdraw/list")
    public R<PageResult<WithdrawRecordVO>> withdrawList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(withdrawService.adminList(status, page, limit));
    }

    @PutMapping("/withdraw/{id}/audit")
    public R<Void> auditWithdraw(@PathVariable Long id,
                                  @RequestParam Integer status,
                                  @RequestParam(required = false) String rejectReason) {
        withdrawService.audit(id, status, rejectReason, UserContext.getUserId());
        return R.ok();
    }

    @PutMapping("/withdraw/{id}/pay")
    public R<Void> markPaid(@PathVariable Long id, @RequestParam String paymentRemark) {
        withdrawService.markPaid(id, paymentRemark);
        return R.ok();
    }

    @GetMapping("/rank")
    public R<List<DistributorRankVO>> rankList(@RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(commissionService.rankList(limit));
    }
}
