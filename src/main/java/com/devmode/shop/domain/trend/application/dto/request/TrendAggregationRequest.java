package com.devmode.shop.domain.trend.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record TrendAggregationRequest(
        @Size(max = 10, message = "키워드는 최대 10개까지 가능합니다")
        List<String> keywords,

        @NotNull(message = "집계 타입은 필수입니다")
        String aggregationType, // "daily", "weekly", "monthly"

        LocalDate startDate,

        LocalDate endDate,

        @Size(max = 10, message = "정렬 기준은 최대 10개까지 가능합니다")
        @NotNull(message = "정렬 기준은 필수입니다")
        List<String> sortBy, // "avgRatio", "totalClick", "trendStrength"

        String sortOrder, // "asc", "desc"

        Integer page,

        Integer size
) {
    public TrendAggregationRequest {
        if (page == null) page = 0;
        if (size == null) size = 20;
        if (sortOrder == null) sortOrder = "desc";
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        
        // 정렬 기준 기본값 설정
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = List.of("avgRatio");
        }
        
        // 정렬 순서 유효성 검사
        if (!sortOrder.equalsIgnoreCase("asc") && !sortOrder.equalsIgnoreCase("desc")) {
            sortOrder = "desc";
        }
        
        // 집계 타입 유효성 검사
        if (aggregationType != null && !aggregationType.matches("^(daily|weekly|monthly)$")) {
            throw new IllegalArgumentException("지원하지 않는 집계 타입: " + aggregationType);
        }
    }
}
