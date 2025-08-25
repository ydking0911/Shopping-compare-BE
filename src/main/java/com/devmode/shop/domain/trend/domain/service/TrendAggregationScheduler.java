package com.devmode.shop.domain.trend.domain.service;

import com.devmode.shop.domain.trend.application.usecase.TrendAggregationUseCase;
import com.devmode.shop.global.config.properties.PrefetchProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrendAggregationScheduler {

    private final TrendAggregationUseCase trendAggregationUseCase;
    private final PrefetchProperties prefetchProperties;

    /**
     * 매일 새벽 3시에 일(日) 집계 수행
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduleDailyAggregation() {
        try {
            log.info("[Scheduler] 일(日) 집계 시작");
            trendAggregationUseCase.aggregateDaily(prefetchProperties.getPopularKeywords());
            log.info("[Scheduler] 일(日) 집계 완료");
        } catch (Exception e) {
            log.error("[Scheduler] 일(日) 집계 실패", e);
        }
    }

    /**
     * 매주 월요일 새벽 4시에 주(週) 집계 수행
     */
    @Scheduled(cron = "0 0 4 ? * MON")
    public void scheduleWeeklyAggregation() {
        try {
            log.info("[Scheduler] 주(週) 집계 시작");
            trendAggregationUseCase.aggregateWeekly(prefetchProperties.getPopularKeywords());
            log.info("[Scheduler] 주(週) 집계 완료");
        } catch (Exception e) {
            log.error("[Scheduler] 주(週) 집계 실패", e);
        }
    }

    /**
     * 매월 1일 새벽 5시에 월(月) 집계 수행
     */
    @Scheduled(cron = "0 0 5 1 * ?")
    public void scheduleMonthlyAggregation() {
        try {
            log.info("[Scheduler] 월(月) 집계 시작");
            trendAggregationUseCase.aggregateMonthly(prefetchProperties.getPopularKeywords());
            log.info("[Scheduler] 월(月) 집계 완료");
        } catch (Exception e) {
            log.error("[Scheduler] 월(月) 집계 실패", e);
        }
    }
}
