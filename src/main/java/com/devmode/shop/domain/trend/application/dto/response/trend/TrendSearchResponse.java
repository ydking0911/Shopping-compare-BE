package com.devmode.shop.domain.trend.application.dto.response.trend;

import java.time.LocalDate;
import java.util.List;
import com.devmode.shop.domain.trend.application.dto.response.trend.TrendDataPoint;
import com.devmode.shop.domain.trend.application.dto.response.trend.TrendSummary;

public record TrendSearchResponse(
        String keyword,
        LocalDate startDate,
        LocalDate endDate,
        String timeUnit,
        String source,
        Long totalDataPoints,
        List<TrendDataPoint> dataPoints,
        TrendSummary summary,
        String cacheStatus,
        Long responseTime,
        Long apiCallCount,
        String quotaStatus
) {}
