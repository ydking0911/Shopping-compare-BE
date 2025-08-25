package com.devmode.shop.domain.trend.application.dto.response;

import java.util.List;

public record TrendAggregationListResponse(
        List<TrendAggregationResponse> aggregations,
        int totalElements,
        int totalPages,
        int currentPage,
        int size,
        boolean hasNext,
        boolean hasPrevious
) {}
