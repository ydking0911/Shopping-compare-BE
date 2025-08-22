package com.devmode.shop.domain.trend.application.dto.response.datalab;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record DataLabDataPoint(
    String period,
    BigDecimal ratio,
    Long clickCount,
    
    @JsonProperty("device")
    List<com.devmode.shop.domain.trend.application.dto.response.datalab.DataLabDistribution> deviceDistribution,
    
    @JsonProperty("gender")
    List<com.devmode.shop.domain.trend.application.dto.response.datalab.DataLabDistribution> genderDistribution,
    
    @JsonProperty("age")
    List<com.devmode.shop.domain.trend.application.dto.response.datalab.DataLabDistribution> ageDistribution
) {}
