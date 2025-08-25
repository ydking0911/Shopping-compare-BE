package com.devmode.shop.domain.trend.domain.service;

import com.devmode.shop.domain.trend.application.usecase.PrefetchTrendsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrendPrefetchScheduler {

    private final PrefetchTrendsUseCase prefetchTrendsUseCase;

    /**
     * 애플리케이션 시작 후 초기 시드 로드
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            log.info("[Scheduler] 초기 시드 로드 시작");
            prefetchTrendsUseCase.loadInitialSeed();
            log.info("[Scheduler] 초기 시드 로드 완료");
        } catch (Exception e) {
            log.error("[Scheduler] 초기 시드 로드 실패", e);
        }
    }

    /**
     * 정기적인 트렌드 프리페치 수행
     */
    @Scheduled(cron = "${prefetch.schedule.prefetch-cron:0 */30 * * * ?}")
    public void scheduleTrendPrefetch() {
        try {
            log.info("[Scheduler] 트렌드 프리페치 시작");
            prefetchTrendsUseCase.prefetchTrends();
            log.info("[Scheduler] 트렌드 프리페치 완료");
        } catch (Exception e) {
            log.error("[Scheduler] 트렌드 프리페치 실패", e);
        }
    }

    /**
     * 매일 새벽 2시에 시드 업데이트
     */
    @Scheduled(cron = "${prefetch.schedule.seed-cron:0 0 2 * * ?}")
    public void scheduleSeedUpdate() {
        try {
            log.info("[Scheduler] 시드 업데이트 시작");
            prefetchTrendsUseCase.loadInitialSeed();
            log.info("[Scheduler] 시드 업데이트 완료");
        } catch (Exception e) {
            log.error("[Scheduler] 시드 업데이트 실패", e);
        }
    }
}


