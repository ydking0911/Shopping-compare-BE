package com.devmode.shop.domain.trend.ui;

import com.devmode.shop.domain.trend.application.dto.request.TrendSearchRequest;
import com.devmode.shop.domain.trend.application.dto.response.trend.TrendSearchResponse;
import com.devmode.shop.domain.trend.application.usecase.TrendSearchUseCase;
import com.devmode.shop.global.annotation.TrendApi;
import com.devmode.shop.global.common.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;

@RestController
@RequestMapping("/api/trends")
public class TrendController implements TrendApi {

    private final TrendSearchUseCase trendSearchUseCase;

    public TrendController(TrendSearchUseCase trendSearchUseCase) {
        this.trendSearchUseCase = trendSearchUseCase;
    }

    @PostMapping("/search")
    @Override
    public BaseResponse<TrendSearchResponse> searchTrends(@Valid @RequestBody TrendSearchRequest request) {
        TrendSearchResponse response = trendSearchUseCase.searchTrends(request);
        return BaseResponse.onSuccess(response);
    }

    @GetMapping("/search")
    @Override
    public BaseResponse<TrendSearchResponse> searchTrendsGet(
            @RequestParam String keyword,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String categories,
            @RequestParam(required = false) String keywords,
            @RequestParam(defaultValue = "date") String timeUnit,
            @RequestParam(defaultValue = "true") Boolean includeDeviceDistribution,
            @RequestParam(defaultValue = "true") Boolean includeGenderDistribution,
            @RequestParam(defaultValue = "true") Boolean includeAgeDistribution) {

        TrendSearchRequest request = new TrendSearchRequest(
                keyword,
                startDate,
                endDate,
                categories != null && !categories.isEmpty()
                        ? Arrays.asList(categories.split(","))
                        : null,
                keywords != null && !keywords.isEmpty()
                        ? Arrays.asList(keywords.split(","))
                        : null,
                timeUnit,
                includeDeviceDistribution,
                includeGenderDistribution,
                includeAgeDistribution
        );

        TrendSearchResponse response = trendSearchUseCase.searchTrends(request);
        return BaseResponse.onSuccess(response);
    }

    @GetMapping("/search/cache")
    @Override
    public BaseResponse<TrendSearchResponse> searchTrendsWithCache(@Valid TrendSearchRequest request) {
        TrendSearchResponse response = trendSearchUseCase.searchTrendsWithCache(request);
        return BaseResponse.onSuccess(response);
    }

    @DeleteMapping("/cache/{keyword}")
    @Override
    public BaseResponse<String> clearTrendCache(@PathVariable String keyword) {
        trendSearchUseCase.clearTrendCache(keyword);
        String message = "트렌드 캐시가 성공적으로 삭제되었습니다: " + keyword;
        return BaseResponse.onSuccess(message);
    }

    @GetMapping("/health")
    @Override
    public BaseResponse<String> healthCheck() {
        String healthStatus = "트렌드 인사이트 서비스가 정상적으로 작동 중입니다.";
        return BaseResponse.onSuccess(healthStatus);
    }
}
