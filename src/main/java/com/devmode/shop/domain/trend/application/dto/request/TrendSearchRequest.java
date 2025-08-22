package com.devmode.shop.domain.trend.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record TrendSearchRequest(
        @NotBlank(message = "키워드는 필수입니다")
        @Size(max = 100, message = "키워드는 100자를 초과할 수 없습니다")
        String keyword,

        @NotNull(message = "시작 날짜는 필수입니다")
        LocalDate startDate,

        @NotNull(message = "종료 날짜는 필수입니다")
        LocalDate endDate,

        @Size(max = 10, message = "카테고리는 최대 10개까지 가능합니다")
        List<String> categories,

        @Size(max = 10, message = "키워드는 최대 10개까지 가능합니다")
        List<String> keywords,

        String timeUnit, // "date", "week", "month"

        Boolean includeDeviceDistribution,

        Boolean includeGenderDistribution,

        Boolean includeAgeDistribution
) {
    public TrendSearchRequest {
        // 기본값 설정
        if (includeDeviceDistribution == null) {
            includeDeviceDistribution = true;
        }
        if (includeGenderDistribution == null) {
            includeGenderDistribution = true;
        }
        if (includeAgeDistribution == null) {
            includeAgeDistribution = true;
        }
    }
}
