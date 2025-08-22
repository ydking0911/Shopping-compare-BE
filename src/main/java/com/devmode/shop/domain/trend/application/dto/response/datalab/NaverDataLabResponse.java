package com.devmode.shop.domain.trend.application.dto.response.datalab;

import java.util.List;

public record NaverDataLabResponse(
    String startDate,
    String endDate,
    String timeUnit,
    List<DataLabResult> results
) {}
