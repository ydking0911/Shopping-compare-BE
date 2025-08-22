package com.devmode.shop.domain.trend.application.dto.response.common;

import java.math.BigDecimal;

public record GenderDistribution(
        BigDecimal maleRatio,
        BigDecimal femaleRatio
) {}
