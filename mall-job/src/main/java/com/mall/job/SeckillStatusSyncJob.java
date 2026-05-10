package com.mall.job;

import com.mall.service.promotion.SeckillActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillStatusSyncJob {

    private final SeckillActivityService seckillActivityService;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void syncSeckillStatus() {
        log.info("[Job] Start seckill activity status sync");
        try {
            seckillActivityService.syncActivityStatus();
            log.info("[Job] Seckill activity status sync completed");
        } catch (Exception e) {
            log.error("[Job] Seckill activity status sync failed", e);
        }
    }
}
