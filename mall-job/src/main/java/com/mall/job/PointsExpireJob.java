package com.mall.job;

import com.mall.service.points.PointsExpireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointsExpireJob {

    private final PointsExpireService pointsExpireService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void expirePoints() {
        log.info("[Job] Start points expiry processing");
        try {
            pointsExpireService.expirePoints();
            log.info("[Job] Points expiry processing completed");
        } catch (Exception e) {
            log.error("[Job] Points expiry processing failed", e);
        }
    }
}
