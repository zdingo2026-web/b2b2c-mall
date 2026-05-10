package com.mall.job;

import com.mall.service.distribution.DistributionSettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommissionUnfreezeJob {

    private final DistributionSettlementService distributionSettlementService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void processUnfreeze() {
        log.info("[Job] Start commission unfreeze processing");
        try {
            distributionSettlementService.processUnfreeze();
            log.info("[Job] Commission unfreeze processing completed");
        } catch (Exception e) {
            log.error("[Job] Commission unfreeze processing failed", e);
        }
    }
}
