package com.devmode.shop.domain.trend.application.dto.response.common;

import java.math.BigDecimal;

public record AgeDistribution(
        BigDecimal age10sRatio,
        BigDecimal age20sRatio,
        BigDecimal age30sRatio,
        BigDecimal age40sRatio,
        BigDecimal age50sRatio,
        BigDecimal age60sRatio
) {}
