package com.devmode.shop.domain.batch.domain.service;

import com.devmode.shop.domain.batch.application.dto.request.BatchJobRequest;
import com.devmode.shop.domain.batch.application.usecase.BatchJobUseCase;
import com.devmode.shop.domain.notification.domain.service.BatchNotificationService;
import com.devmode.shop.global.config.properties.BatchProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobSchedulerService {

    private final BatchJobUseCase batchJobUseCase;
    private final BatchProperties batchProperties;
    private final BatchNotificationService notificationService;
    private final ScheduledExecutorService scheduledExecutorService;

    // 재시도 카운터
    private final AtomicInteger prefetchRetryCount = new AtomicInteger(0);
    private final AtomicInteger aggregationRetryCount = new AtomicInteger(0);

    /**
     * 프리페치 트렌드 배치 잡 스케줄링
     */
    @Scheduled(cron = "${batch.prefetch.cron:0 0/30 * * * *}")
    public void schedulePrefetchTrendsJob() {
        if (!batchProperties.getPrefetch().isEnabled()) {
            log.debug("[Batch] 프리페치 잡이 비활성화되어 있습니다.");
            return;
        }

        try {
            BatchJobRequest request = new BatchJobRequest(
                    "prefetch",
                    "스케줄된 프리페치 트렌드 잡",
                    LocalDateTime.now(),
                    Map.of("source", "scheduled"),
                    "scheduled"
            );

            log.info("[Batch] 프리페치 트렌드 잡 스케줄링 시작");
            batchJobUseCase.executePrefetchJob(request);
            
            // 성공 시 재시도 카운터 리셋
            prefetchRetryCount.set(0);
            log.info("[Batch] 프리페치 트렌드 잡 스케줄링 완료");
            
        } catch (Exception e) {
            log.error("[Batch] 프리페치 잡 스케줄링 실패: {}", e.getMessage(), e);
            handlePrefetchFailure(e);
        }
    }

    /**
     * 일별 집계 배치 잡 스케줄링
     */
    @Scheduled(cron = "${batch.aggregation.daily-cron:0 0 3 * * ?}")
    public void scheduleDailyAggregationJob() {
        if (!batchProperties.getAggregation().isEnabled()) {
            log.debug("[Batch] 집계 잡이 비활성화되어 있습니다.");
            return;
        }

        try {
            BatchJobRequest request = new BatchJobRequest(
                    "daily-aggregation",
                    "스케줄된 일별 집계 잡",
                    LocalDateTime.now(),
                    Map.of("source", "scheduled"),
                    "scheduled"
            );

            log.info("[Batch] 일별 집계 잡 스케줄링 시작");
            batchJobUseCase.executeDailyAggregationJob(request);
            
            // 성공 시 재시도 카운터 리셋
            aggregationRetryCount.set(0);
            log.info("[Batch] 일별 집계 잡 스케줄링 완료");
            
        } catch (Exception e) {
            log.error("[Batch] 일별 집계 잡 스케줄링 실패: {}", e.getMessage(), e);
            handleAggregationFailure(e);
        }
    }

    /**
     * 주별 집계 배치 잡 스케줄링
     */
    @Scheduled(cron = "${batch.aggregation.weekly-cron:0 0 4 ? * MON}")
    public void scheduleWeeklyAggregationJob() {
        if (!batchProperties.getAggregation().isEnabled()) {
            log.debug("[Batch] 집계 잡이 비활성화되어 있습니다.");
            return;
        }

        try {
            BatchJobRequest request = new BatchJobRequest(
                    "weekly-aggregation",
                    "스케줄된 주별 집계 잡",
                    LocalDateTime.now(),
                    Map.of("source", "scheduled"),
                    "scheduled"
            );

            log.info("[Batch] 주별 집계 잡 스케줄링 시작");
            batchJobUseCase.executeWeeklyAggregationJob(request);
            
            aggregationRetryCount.set(0);
            log.info("[Batch] 주별 집계 잡 스케줄링 완료");
            
        } catch (Exception e) {
            log.error("[Batch] 주별 집계 잡 스케줄링 실패: {}", e.getMessage(), e);
            handleAggregationFailure(e);
        }
    }

    /**
     * 월별 집계 배치 잡 스케줄링
     */
    @Scheduled(cron = "${batch.aggregation.monthly-cron:0 0 5 1 * ?}")
    public void scheduleMonthlyAggregationJob() {
        if (!batchProperties.getAggregation().isEnabled()) {
            log.debug("[Batch] 집계 잡이 비활성화되어 있습니다.");
            return;
        }

        try {
            BatchJobRequest request = new BatchJobRequest(
                    "monthly-aggregation",
                    "스케줄된 월별 집계 잡",
                    LocalDateTime.now(),
                    Map.of("source", "scheduled"),
                    "scheduled"
            );

            log.info("[Batch] 월별 집계 잡 스케줄링 시작");
            batchJobUseCase.executeMonthlyAggregationJob(request);
            
            aggregationRetryCount.set(0);
            log.info("[Batch] 월별 집계 잡 스케줄링 완료");
            
        } catch (Exception e) {
            log.error("[Batch] 월별 집계 잡 스케줄링 실패: {}", e.getMessage(), e);
            handleAggregationFailure(e);
        }
    }

    /**
     * 프리페치 실패 처리
     */
    private void handlePrefetchFailure(Exception e) {
        int retryCount = prefetchRetryCount.incrementAndGet();
        int maxRetries = batchProperties.getPrefetch().getMaxRetries();
        
        if (retryCount <= maxRetries) {
            log.warn("[Batch] 프리페치 잡 재시도 {}/{}: {}", retryCount, maxRetries, e.getMessage());
            
            // 백오프 지연 계산 (지수 백오프 적용)
            long delayMs = Math.min(
                batchProperties.getPrefetch().getRetryInterval().toMillis() * (1L << (retryCount - 1)),
                batchProperties.getPrefetch().getRetryInterval().toMillis() * 10 // 최대 10배까지만
            );
            
            // Non-blocking 재시도 using ScheduledExecutorService
            scheduledExecutorService.schedule(() -> {
                try {
                    log.info("[Batch] 프리페치 잡 재시도 실행: {}/{}", retryCount, maxRetries);
                    batchJobUseCase.executePrefetchJob(new BatchJobRequest(
                        "prefetch-retry",
                        "프리페치 트렌드 잡 재시도",
                        LocalDateTime.now(),
                        Map.of("source", "retry", "retryCount", String.valueOf(retryCount)),
                        "retry"
                    ));
                } catch (Exception ex) {
                    log.error("[Batch] 프리페치 잡 재시도 중 예외 발생", ex);
                }
            }, delayMs, TimeUnit.MILLISECONDS);
            
        } else {
            log.error("[Batch] 프리페치 잡 최대 재시도 횟수 초과: {}", maxRetries);
            
            if (batchProperties.getCommon().isFailureNotificationEnabled()) {
                // 실패 알림 발송
                Map<String, Object> context = Map.of(
                    "retryCount", retryCount,
                    "maxRetries", maxRetries,
                    "lastError", e.getMessage(),
                    "timestamp", LocalDateTime.now()
                );
                notificationService.sendFailureNotification("prefetchTrendsJob", e.getMessage(), context);
            }
        }
    }

    /**
     * 집계 실패 처리
     */
    private void handleAggregationFailure(Exception e) {
        int retryCount = aggregationRetryCount.incrementAndGet();
        int maxRetries = batchProperties.getAggregation().getMaxRetries();
        
        if (retryCount <= maxRetries) {
            log.warn("[Batch] 집계 잡 재시도 {}/{}: {}", retryCount, maxRetries, e.getMessage());
            
            // 백오프 지연 계산 (지수 백오프 적용)
            long delayMs = Math.min(
                batchProperties.getAggregation().getRetryInterval().toMillis() * (1L << (retryCount - 1)),
                batchProperties.getAggregation().getRetryInterval().toMillis() * 10 // 최대 10배까지만
            );
            
            // Non-blocking 재시도 using ScheduledExecutorService
            scheduledExecutorService.schedule(() -> {
                try {
                    log.info("[Batch] 집계 잡 재시도 실행: {}/{}", retryCount, maxRetries);
                    batchJobUseCase.executeDailyAggregationJob(new BatchJobRequest(
                        "daily-aggregation-retry",
                        "일별 집계 잡 재시도",
                        LocalDateTime.now(),
                        Map.of("source", "retry", "retryCount", String.valueOf(retryCount)),
                        "retry"
                    ));
                } catch (Exception ex) {
                    log.error("[Batch] 집계 잡 재시도 중 예외 발생", ex);
                }
            }, delayMs, TimeUnit.MILLISECONDS);
            
        } else {
            log.error("[Batch] 집계 잡 최대 재시도 횟수 초과: {}", maxRetries);
            
            if (batchProperties.getCommon().isFailureNotificationEnabled()) {
                // 실패 알림 발송
                Map<String, Object> context = Map.of(
                    "retryCount", retryCount,
                    "maxRetries", maxRetries,
                    "lastError", e.getMessage(),
                    "timestamp", LocalDateTime.now()
                );
                notificationService.sendFailureNotification("aggregationJob", e.getMessage(), context);
            }
        }
    }
}
