package com.devmode.shop.domain.monitoring.domain.service;

import com.devmode.shop.domain.monitoring.application.dto.response.HealthStatusResponse;
import com.devmode.shop.global.config.properties.MonitoringProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MonitoringProperties monitoringProperties;

    // 메트릭 수집
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final AtomicLong maxResponseTime = new AtomicLong(0);

    // 배치 잡 관련 메트릭
    private final AtomicLong batchJobSuccessCount = new AtomicLong(0);
    private final AtomicLong batchJobFailureCount = new AtomicLong(0);
    private final AtomicLong batchJobExecutionTime = new AtomicLong(0);
    private final AtomicInteger activeBatchJobs = new AtomicInteger(0);

    // 집계 관련 메트릭
    private final AtomicLong aggregationSuccesses = new AtomicLong(0);
    private final AtomicLong aggregationFailures = new AtomicLong(0);
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);

    private LocalDateTime lastCacheStatsTime = LocalDateTime.now();

    /**
     * 요청 메트릭 기록
     */
    public void recordRequest() {
        totalRequests.incrementAndGet();
    }

    /**
     * 캐시 히트 메트릭 기록
     */
    public void recordCacheHit() {
        cacheHits.incrementAndGet();
    }

    /**
     * 캐시 미스 메트릭 기록
     */
    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
    }

    /**
     * 집계 성공 메트릭 기록
     */
    public void recordAggregationSuccess() {
        aggregationSuccesses.incrementAndGet();
        consecutiveFailures.set(0);
    }

    /**
     * 집계 실패 메트릭 기록
     */
    public void recordAggregationFailure() {
        aggregationFailures.incrementAndGet();
        int consecutive = consecutiveFailures.incrementAndGet();
        
        if (consecutive >= monitoringProperties.getAlert().getAggregationFailureThreshold()) {
            log.error("[Monitoring] 집계 연속 실패 임계값 초과: {}회", consecutive);
            // TODO: 알림 시스템 연동 (Slack, Email 등)
        }
    }

    /**
     * 응답 시간 메트릭 기록
     */
    public void recordResponseTime(long responseTimeMs) {
        totalResponseTime.addAndGet(responseTimeMs);
        
        // AtomicLong.updateAndGet()을 사용하여 busy-wait loop 방지
        maxResponseTime.updateAndGet(currentMax -> 
            Math.max(currentMax, responseTimeMs)
        );
    }

    /**
     * 배치 잡 성공 기록
     */
    public void recordBatchJobSuccess() {
        batchJobSuccessCount.incrementAndGet();
        recordAggregationSuccess(); // 기존 집계 성공 메트릭과 연동
    }

    /**
     * 배치 잡 실패 기록
     */
    public void recordBatchJobFailure() {
        batchJobFailureCount.incrementAndGet();
        recordAggregationFailure(); // 기존 집계 실패 메트릭과 연동
    }

    /**
     * 배치 잡 실행 시간 기록
     */
    public void recordBatchJobExecutionTime(long executionTimeMs) {
        batchJobExecutionTime.addAndGet(executionTimeMs);
    }

    /**
     * 활성 배치 잡 수 증가
     */
    public void incrementActiveBatchJobs() {
        activeBatchJobs.incrementAndGet();
    }

    /**
     * 활성 배치 잡 수 감소
     */
    public void decrementActiveBatchJobs() {
        activeBatchJobs.decrementAndGet();
    }

    /**
     * 캐시 히트율 계산
     */
    public double getCacheHitRate() {
        long total = cacheHits.get() + cacheMisses.get();
        return total > 0 ? (double) cacheHits.get() / total : 0.0;
    }

    /**
     * 평균 응답 시간 계산
     */
    public double getAverageResponseTime() {
        long total = totalRequests.get();
        return total > 0 ? (double) totalResponseTime.get() / total : 0.0;
    }

    /**
     * 배치 잡 성공률 조회
     */
    public double getBatchJobSuccessRate() {
        long totalJobs = batchJobSuccessCount.get() + batchJobFailureCount.get();
        if (totalJobs == 0) {
            return 0.0;
        }
        return (double) batchJobSuccessCount.get() / totalJobs;
    }

    /**
     * 평균 배치 잡 실행 시간 조회
     */
    public long getAverageBatchJobExecutionTime() {
        long totalJobs = batchJobSuccessCount.get() + batchJobFailureCount.get();
        if (totalJobs == 0) {
            return 0L;
        }
        return batchJobExecutionTime.get() / totalJobs;
    }

    /**
     * 활성 배치 잡 수 조회
     */
    public int getActiveBatchJobs() {
        return activeBatchJobs.get();
    }

    /**
     * 배치 잡 성공 수 조회
     */
    public long getBatchJobSuccessCount() {
        return batchJobSuccessCount.get();
    }

    /**
     * 배치 잡 실패 수 조회
     */
    public long getBatchJobFailureCount() {
        return batchJobFailureCount.get();
    }

    /**
     * 최대 응답 시간 조회
     */
    public long getMaxResponseTime() {
        return maxResponseTime.get();
    }

    /**
     * 요청 수 조회
     */
    public long getRequestCount() {
        return totalRequests.get();
    }

    /**
     * 캐시 히트 수 조회
     */
    public long getCacheHitCount() {
        return cacheHits.get();
    }

    /**
     * 캐시 미스 수 조회
     */
    public long getCacheMissCount() {
        return cacheMisses.get();
    }

    /**
     * 집계 성공 수 조회
     */
    public long getAggregationSuccessCount() {
        return aggregationSuccesses.get();
    }

    /**
     * 집계 실패 수 조회
     */
    public long getAggregationFailureCount() {
        return aggregationFailures.get();
    }

    /**
     * 연속 실패 수 조회
     */
    public int getConsecutiveFailures() {
        return consecutiveFailures.get();
    }

    /**
     * 메트릭 통계 로깅
     */
    @Scheduled(fixedRate = 300000) // 5분마다
    public void logMetrics() {
        if (!monitoringProperties.isMetricsEnabled()) {
            return;
        }

        double hitRate = getCacheHitRate();
        double avgResponseTime = getAverageResponseTime();
        long maxResponseTimeMs = maxResponseTime.get();

        log.info("[Monitoring] 메트릭 통계 - " +
                "총 요청: {}, 캐시 히트율: {:.2%}, " +
                "평균 응답시간: {:.2f}ms, 최대 응답시간: {}ms, " +
                "집계 성공: {}, 집계 실패: {}, 연속 실패: {}",
                totalRequests.get(), hitRate, avgResponseTime, maxResponseTimeMs,
                aggregationSuccesses.get(), aggregationFailures.get(), consecutiveFailures.get());

        // 캐시 히트율 알림
        if (hitRate < monitoringProperties.getAlert().getCacheHitRateAlertThreshold()) {
            log.warn("[Monitoring] 캐시 히트율 저하: {:.2%} (임계값: {:.2%})", 
                    hitRate, monitoringProperties.getAlert().getCacheHitRateAlertThreshold());
        }

        // 응답 시간 알림
        if (avgResponseTime > monitoringProperties.getAlert().getResponseTimeThreshold()) {
            log.warn("[Monitoring] 응답 시간 임계값 초과: {:.2f}ms (임계값: {}ms)", 
                    avgResponseTime, monitoringProperties.getAlert().getResponseTimeThreshold());
        }
    }

    /**
     * 캐시 상태 모니터링
     */
    @Scheduled(fixedRate = 300000) // 5분마다
    public void monitorCacheHealth() {
        try {
            String pattern = "trend:agg:*";
            var keys = redisTemplate.keys(pattern);
            
            if (keys != null) {
                int keyCount = keys.size();
                log.info("[Monitoring] 캐시 상태 - 총 키 개수: {}", keyCount);
                
                if (keyCount > monitoringProperties.getCache().getMaxKeyCount()) {
                    log.warn("[Monitoring] 캐시 키 개수 임계값 초과: {} (임계값: {})", 
                            keyCount, monitoringProperties.getCache().getMaxKeyCount());
                }
                
                // 타입별 키 개수 분석
                long dailyCount = keys.stream().filter(key -> key.contains(":daily:")).count();
                long weeklyCount = keys.stream().filter(key -> key.contains(":weekly:")).count();
                long monthlyCount = keys.stream().filter(key -> key.contains(":monthly:")).count();
                
                log.debug("[Monitoring] 캐시 타입별 키 분포 - daily: {}, weekly: {}, monthly: {}", 
                        dailyCount, weeklyCount, monthlyCount);
            }
            
            lastCacheStatsTime = LocalDateTime.now();
            
        } catch (Exception e) {
            log.error("[Monitoring] 캐시 상태 모니터링 실패", e);
        }
    }

    /**
     * 메트릭 초기화 (일일)
     */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정
    public void resetDailyMetrics() {
        totalRequests.set(0);
        cacheHits.set(0);
        cacheMisses.set(0);
        totalResponseTime.set(0);
        maxResponseTime.set(0);
        aggregationSuccesses.set(0);
        aggregationFailures.set(0);
        consecutiveFailures.set(0);
        batchJobSuccessCount.set(0);
        batchJobFailureCount.set(0);
        batchJobExecutionTime.set(0);
        activeBatchJobs.set(0);
        log.info("[Monitoring] 일일 메트릭 초기화 완료");
    }

    /**
     * 헬스체크 상태 확인
     */
    public boolean isHealthy() {
        try {
            // Redis 연결 상태 확인
            redisTemplate.opsForValue().get("health_check");
            
            // 기본 메트릭 상태 확인
            double hitRate = getCacheHitRate();
            double avgResponseTime = getAverageResponseTime();
            
            return hitRate >= monitoringProperties.getCache().getHitRateThreshold() &&
                   avgResponseTime <= monitoringProperties.getAlert().getResponseTimeThreshold() &&
                   consecutiveFailures.get() < monitoringProperties.getAlert().getAggregationFailureThreshold();
                   
        } catch (Exception e) {
            log.error("[Monitoring] 헬스체크 실패", e);
            return false;
        }
    }

    /**
     * 상세 헬스체크 정보
     */
    public HealthStatusResponse getDetailedHealthStatus() {
        return new HealthStatusResponse(
                isHealthy(),
                getCacheHitRate(),
                (long) getAverageResponseTime(),
                getRequestCount(),
                getCacheHitCount(),
                getCacheMissCount(),
                getAggregationSuccessCount(),
                getAggregationFailureCount(),
                getConsecutiveFailures(),
                lastCacheStatsTime,
                getBatchJobSuccessRate(),
                getAverageBatchJobExecutionTime(),
                getActiveBatchJobs()
        );
    }
}
