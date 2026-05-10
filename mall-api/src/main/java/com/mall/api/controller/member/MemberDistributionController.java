package com.mall.api.controller.member;

import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.dto.distribution.DistributorApplyDTO;
import com.mall.model.dto.distribution.WithdrawApplyDTO;
import com.mall.model.vo.distribution.*;
import com.mall.service.distribution.CommissionService;
import com.mall.service.distribution.DistributionProductService;
import com.mall.service.distribution.DistributionRelationService;
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
@RequestMapping("/api/v1/member/distribution")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class MemberDistributionController {

    private final DistributorService distributorService;
    private final DistributionProductService distributionProductService;
    private final CommissionService commissionService;
    private final WithdrawService withdrawService;
    private final DistributionRelationService distributionRelationService;

    @PostMapping("/apply")
    public R<Void> apply(@Validated @RequestBody DistributorApplyDTO dto) {
        Long memberId = UserContext.getUserId();
        distributorService.apply(memberId, dto);
        return R.ok();
    }

    @GetMapping("/center")
    public R<DistributionCenterVO> center() {
        Long memberId = UserContext.getUserId();
        return R.ok(distributorService.center(memberId));
    }

    @GetMapping("/product/list")
    public R<PageResult<DistributionProductVO>> products(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(distributionProductService.list(null, keyword, page, limit));
    }

    @GetMapping("/commission/list")
    public R<PageResult<CommissionRecordVO>> commissionList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long memberId = UserContext.getUserId();
        Long distributorId = distributorService.getDistributor(memberId) != null
                ? distributorService.getDistributor(memberId).getId() : null;
        return R.ok(commissionService.commissionList(distributorId, status, page, limit));
    }

    @GetMapping("/order/list")
    public R<PageResult<DistributionOrderVO>> orderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long memberId = UserContext.getUserId();
        Long distributorId = distributorService.getDistributor(memberId) != null
                ? distributorService.getDistributor(memberId).getId() : null;
        return R.ok(commissionService.orderList(distributorId, page, limit));
    }

    @GetMapping("/stat")
    public R<DistributionStatVO> stat() {
        Long memberId = UserContext.getUserId();
        Long distributorId = distributorService.getDistributor(memberId) != null
                ? distributorService.getDistributor(memberId).getId() : null;
        return R.ok(commissionService.stat(distributorId));
    }

    @GetMapping("/trend")
    public R<List<CommissionTrendVO>> trend(@RequestParam(defaultValue = "7") Integer days) {
        Long memberId = UserContext.getUserId();
        Long distributorId = distributorService.getDistributor(memberId) != null
                ? distributorService.getDistributor(memberId).getId() : null;
        return R.ok(commissionService.commissionTrend(distributorId, days));
    }

    @PostMapping("/withdraw")
    public R<Void> applyWithdraw(@Validated @RequestBody WithdrawApplyDTO dto) {
        Long memberId = UserContext.getUserId();
        Long distributorId = distributorService.getDistributor(memberId) != null
                ? distributorService.getDistributor(memberId).getId() : null;
        withdrawService.apply(distributorId, dto);
        return R.ok();
    }

    @GetMapping("/withdraw/list")
    public R<PageResult<WithdrawRecordVO>> withdrawList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long memberId = UserContext.getUserId();
        Long distributorId = distributorService.getDistributor(memberId) != null
                ? distributorService.getDistributor(memberId).getId() : null;
        return R.ok(withdrawService.list(distributorId, status, page, limit));
    }

    @GetMapping("/team")
    public R<PageResult<DistributorVO>> teamList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long memberId = UserContext.getUserId();
        Long distributorId = distributorService.getDistributor(memberId) != null
                ? distributorService.getDistributor(memberId).getId() : null;
        return R.ok(distributorService.teamList(distributorId, page, limit));
    }
}
