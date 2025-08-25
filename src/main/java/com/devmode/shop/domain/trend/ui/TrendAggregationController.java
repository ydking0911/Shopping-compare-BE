package com.devmode.shop.domain.trend.ui;

import com.devmode.shop.domain.trend.application.dto.request.TrendAggregationRequest;
import com.devmode.shop.domain.trend.application.dto.response.TrendAggregationListResponse;
import com.devmode.shop.domain.trend.application.usecase.TrendAggregationUseCase;
import com.devmode.shop.domain.trend.domain.service.TrendAggregationCacheService;
import com.devmode.shop.global.common.BaseResponse;
import com.devmode.shop.global.config.properties.PrefetchProperties;
import com.devmode.shop.global.swagger.TrendAggregationApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TrendAggregationController implements TrendAggregationApi {

    private final TrendAggregationUseCase trendAggregationUseCase;
    private final TrendAggregationCacheService cacheService;
    private final PrefetchProperties prefetchProperties;

    @Override
    public BaseResponse<Void> aggregateDaily() {
        trendAggregationUseCase.aggregateDaily(prefetchProperties.getPopularKeywords());
        return BaseResponse.onSuccess();
    }

    @Override
    public BaseResponse<Void> aggregateWeekly() {
        trendAggregationUseCase.aggregateWeekly(prefetchProperties.getPopularKeywords());
        return BaseResponse.onSuccess();
    }

    @Override
    public BaseResponse<Void> aggregateMonthly() {
        trendAggregationUseCase.aggregateMonthly(prefetchProperties.getPopularKeywords());
        return BaseResponse.onSuccess();
    }

    @Override
    public BaseResponse<TrendAggregationListResponse> queryAggregations(TrendAggregationRequest request) {
        TrendAggregationListResponse response = trendAggregationUseCase.queryAggregations(request);
        return BaseResponse.onSuccess(response);
    }

    @Override
    public BaseResponse<Void> getCacheStats() {
        cacheService.logCacheStats();
        return BaseResponse.onSuccess();
    }

    @Override
    public BaseResponse<Void> invalidateCacheByKeyword(String keyword) {
        cacheService.invalidateCacheByKeyword(keyword);
        return BaseResponse.onSuccess();
    }

    @Override
    public BaseResponse<Void> invalidateCacheByType(String type) {
        cacheService.invalidateCacheByType(type);
        return BaseResponse.onSuccess();
    }

    @Override
    public BaseResponse<Void> invalidateAllCache() {
        cacheService.invalidateAllCache();
        return BaseResponse.onSuccess();
    }
}


