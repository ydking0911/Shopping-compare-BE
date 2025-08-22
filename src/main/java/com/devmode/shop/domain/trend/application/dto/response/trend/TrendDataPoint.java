package com.devmode.shop.domain.trend.application.dto.response.trend;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.devmode.shop.domain.trend.application.dto.response.common.DeviceDistribution;
import com.devmode.shop.domain.trend.application.dto.response.common.GenderDistribution;
import com.devmode.shop.domain.trend.application.dto.response.common.AgeDistribution;

public record TrendDataPoint(
        LocalDate date,
        BigDecimal ratio,
        Long clickCount,
        DeviceDistribution deviceDistribution,
        GenderDistribution genderDistribution,
        AgeDistribution ageDistribution
) {}
