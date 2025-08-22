package com.devmode.shop.domain.trend.application.dto.response.datalab;

import java.util.List;

public record DataLabResult(
    String title,
    List<DataLabDataPoint> data
) {}
