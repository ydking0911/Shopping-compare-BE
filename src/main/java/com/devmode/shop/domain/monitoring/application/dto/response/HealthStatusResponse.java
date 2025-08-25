package com.devmode.shop.domain.monitoring.application.dto.response;

import java.time.LocalDateTime;

public record HealthStatusResponse(
        boolean healthy,
        double cacheHitRate,
        long averageResponseTime,
        long requestCount,
        long cacheHitCount,
        long cacheMissCount,
        long aggregationSuccessCount,
        long aggregationFailureCount,
        int consecutiveFailures,
        LocalDateTime lastCacheStatsTime,
        double batchJobSuccessRate,
        long averageBatchJobExecutionTime,
        int activeBatchJobs
) {}
