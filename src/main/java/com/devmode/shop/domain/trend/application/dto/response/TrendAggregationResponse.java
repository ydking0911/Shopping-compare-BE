package com.devmode.shop.domain.trend.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TrendAggregationResponse(
        String keyword,
        LocalDate date,
        BigDecimal avgRatio,
        BigDecimal maxRatio,
        BigDecimal minRatio,
        Long totalClickCount,
        String trendDirection,
        BigDecimal trendStrength
) {}


