package com.mall.service.job;

import com.mall.service.distribution.DistributionSettlementService;
import com.mall.service.points.PointsExpireService;
import com.mall.service.promotion.SeckillActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Phase2ScheduledJobs {

    private final PointsExpireService pointsExpireService;
    private final DistributionSettlementService distributionSettlementService;
    private final SeckillActivityService seckillActivityService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void expirePoints() {
        log.info("[Job] Start points expiry processing");
        try {
            pointsExpireService.expirePoints();
        } catch (Exception e) {
            log.error("[Job] Points expiry failed", e);
        }
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void processCommissionUnfreeze() {
        log.info("[Job] Start commission unfreeze processing");
        try {
            distributionSettlementService.processUnfreeze();
        } catch (Exception e) {
            log.error("[Job] Commission unfreeze failed", e);
        }
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    public void syncSeckillStatus() {
        log.info("[Job] Start seckill activity status sync");
        try {
            seckillActivityService.syncActivityStatus();
        } catch (Exception e) {
            log.error("[Job] Seckill status sync failed", e);
        }
    }
}
