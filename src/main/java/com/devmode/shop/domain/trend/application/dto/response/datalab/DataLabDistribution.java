package com.devmode.shop.domain.trend.application.dto.response.datalab;

import java.math.BigDecimal;

public record DataLabDistribution(
    String key,
    BigDecimal ratio
) {}
