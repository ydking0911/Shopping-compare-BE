package com.devmode.shop.global.annotation;

import com.devmode.shop.domain.trend.application.dto.request.TrendSearchRequest;
import com.devmode.shop.domain.trend.application.dto.response.trend.TrendSearchResponse;
import com.devmode.shop.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Trend API", description = "트렌드 인사이트 관련 API")
public interface TrendApi {

    @PostMapping("/search")
    @Operation(summary = "트렌드 검색 (POST)", description = "POST 방식으로 트렌드 데이터를 검색합니다.")
    BaseResponse<TrendSearchResponse> searchTrends(@RequestBody TrendSearchRequest request);

    @GetMapping("/search")
    @Operation(summary = "트렌드 검색 (GET)", description = "GET 방식으로 트렌드 데이터를 검색합니다.")
    BaseResponse<TrendSearchResponse> searchTrendsGet(
            @RequestParam String keyword,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) String categories,
            @RequestParam(required = false) String keywords,
            @RequestParam(defaultValue = "date") String timeUnit,
            @RequestParam(defaultValue = "true") Boolean includeDeviceDistribution,
            @RequestParam(defaultValue = "true") Boolean includeGenderDistribution,
            @RequestParam(defaultValue = "true") Boolean includeAgeDistribution
    );

    @GetMapping("/search/cache")
    @Operation(summary = "캐시 우선 트렌드 검색", description = "캐시된 데이터를 우선적으로 사용하여 트렌드를 검색합니다.")
    BaseResponse<TrendSearchResponse> searchTrendsWithCache(TrendSearchRequest request);

    @DeleteMapping("/cache/{keyword}")
    @Operation(summary = "트렌드 캐시 삭제", description = "특정 키워드의 트렌드 캐시를 삭제합니다.")
    BaseResponse<String> clearTrendCache(@PathVariable String keyword);

    @GetMapping("/health")
    @Operation(summary = "트렌드 서비스 상태 확인", description = "트렌드 인사이트 서비스의 상태를 확인합니다.")
    BaseResponse<String> healthCheck();
}
