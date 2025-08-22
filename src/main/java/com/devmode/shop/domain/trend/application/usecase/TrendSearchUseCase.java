package com.devmode.shop.domain.trend.application.usecase;

import com.devmode.shop.domain.trend.application.dto.request.TrendSearchRequest;
import com.devmode.shop.domain.trend.application.dto.response.datalab.NaverDataLabResponse;
import com.devmode.shop.domain.trend.application.dto.response.trend.TrendSearchResponse;
import com.devmode.shop.domain.trend.domain.service.NaverDataLabApiService;
import com.devmode.shop.domain.trend.domain.service.TrendCacheService;
import com.devmode.shop.domain.trend.domain.service.TrendTransformService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TrendSearchUseCase {

    private final NaverDataLabApiService naverDataLabApiService;
    private final TrendCacheService trendCacheService;
    private final TrendTransformService transformService;

    public TrendSearchResponse searchTrends(TrendSearchRequest request) {
        long startTime = System.currentTimeMillis();

        // 1. 캐시 확인
        if (trendCacheService.isCached(request)) {
            return trendCacheService.getCachedSearchResult(request)
                    .orElseThrow(() -> new RuntimeException("Failed to retrieve cached result"));
        }

        try {
            // 2. 네이버 DataLab API 호출
            NaverDataLabResponse naverResponse = naverDataLabApiService.searchTrends(request);

            // 3. 응답 변환
            TrendSearchResponse response = transformService.transformToTrendSearchResponse(
                    naverResponse,
                    request,
                    "fresh",
                    System.currentTimeMillis() - startTime,
                    1L, // API 호출 횟수
                    "available" // 쿼터 상태
            );

            // 4. 캐시 저장
            trendCacheService.cacheSearchResult(request, response);

            return response;

        } catch (Exception e) {
            // 5. 캐시 폴백 시도
            try {
                return trendCacheService.getCachedSearchResult(request)
                        .orElseThrow(() -> new RuntimeException("No cached result available for fallback"));
            } catch (Exception fallbackException) {
                throw new RuntimeException("Trend search failed and no fallback available", e);
            }
        }
    }

    public TrendSearchResponse searchTrendsWithCache(TrendSearchRequest request) {
        // 캐시 우선 검색
        return trendCacheService.getCachedSearchResult(request)
                .orElseGet(() -> searchTrends(request));
    }

    public void clearTrendCache(String keyword) {
        trendCacheService.clearCache(keyword);
        trendCacheService.clearSearchCache(keyword);
    }
}
