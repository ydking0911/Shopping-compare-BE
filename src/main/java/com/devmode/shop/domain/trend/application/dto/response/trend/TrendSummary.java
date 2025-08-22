package com.devmode.shop.domain.trend.application.dto.response.trend;

import java.math.BigDecimal;

public record TrendSummary(
        BigDecimal avgRatio,
        BigDecimal maxRatio,
        BigDecimal minRatio,
        Long totalClickCount,
        String trendDirection,
        BigDecimal trendStrength
) {}
