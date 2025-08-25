package com.devmode.shop.domain.trend.ui;

import com.devmode.shop.domain.trend.application.usecase.PrefetchTrendsUseCase;
import com.devmode.shop.global.common.BaseResponse;
import com.devmode.shop.global.swagger.TrendPrefetchApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TrendPrefetchController implements TrendPrefetchApi {

    private final PrefetchTrendsUseCase prefetchTrendsUseCase;

    @Override
    public BaseResponse<Void> seedFromDataLab() {
        prefetchTrendsUseCase.loadInitialSeed();
        return BaseResponse.onSuccess();
    }

    @Override
    public BaseResponse<Void> prefetch() {
        prefetchTrendsUseCase.prefetchTrends();
        return BaseResponse.onSuccess();
    }
}


