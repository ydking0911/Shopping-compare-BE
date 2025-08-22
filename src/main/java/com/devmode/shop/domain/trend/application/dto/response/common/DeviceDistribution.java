package com.devmode.shop.domain.trend.application.dto.response.common;

import java.math.BigDecimal;

public record DeviceDistribution(
        BigDecimal mobileRatio,
        BigDecimal pcRatio,
        BigDecimal tabletRatio
) {}
