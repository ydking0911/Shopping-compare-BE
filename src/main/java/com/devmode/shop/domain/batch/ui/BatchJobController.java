package com.devmode.shop.domain.batch.ui;

import com.devmode.shop.domain.batch.application.dto.request.BatchJobRequest;
import com.devmode.shop.domain.batch.application.dto.response.BatchJobResponse;
import com.devmode.shop.domain.batch.application.usecase.BatchJobUseCase;
import com.devmode.shop.global.common.BaseResponse;
import com.devmode.shop.global.config.properties.BatchProperties;
import com.devmode.shop.global.swagger.BatchJobApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BatchJobController implements BatchJobApi {

    private final BatchJobUseCase batchJobUseCase;
    private final BatchProperties batchProperties;

    @Override
    public BaseResponse<String> triggerPrefetchJob() {
        BatchJobRequest request = new BatchJobRequest(
                "prefetch",
                "프리페치 트렌드 잡 수동 실행",
                LocalDateTime.now(),
                Map.of("source", "manual"),
                "manual"
        );

        BatchJobResponse response = batchJobUseCase.executePrefetchJob(request);
        return BaseResponse.onSuccess(response.message());
    }

    @Override
    public BaseResponse<String> triggerDailyAggregationJob() {
        BatchJobRequest request = new BatchJobRequest(
                "daily-aggregation",
                "일별 집계 잡 수동 실행",
                LocalDateTime.now(),
                Map.of("source", "manual"),
                "manual"
        );

        BatchJobResponse response = batchJobUseCase.executeDailyAggregationJob(request);
        return BaseResponse.onSuccess(response.message());
    }

    @Override
    public BaseResponse<String> triggerWeeklyAggregationJob() {
        BatchJobRequest request = new BatchJobRequest(
                "weekly-aggregation",
                "주별 집계 잡 수동 실행",
                LocalDateTime.now(),
                Map.of("source", "manual"),
                "manual"
        );

        BatchJobResponse response = batchJobUseCase.executeWeeklyAggregationJob(request);
        return BaseResponse.onSuccess(response.message());
    }

    @Override
    public BaseResponse<String> triggerMonthlyAggregationJob() {
        BatchJobRequest request = new BatchJobRequest(
                "monthly-aggregation",
                "월별 집계 잡 수동 실행",
                LocalDateTime.now(),
                Map.of("source", "manual"),
                "manual"
        );

        BatchJobResponse response = batchJobUseCase.executeMonthlyAggregationJob(request);
        return BaseResponse.onSuccess(response.message());
    }

    @Override
    public BaseResponse<BatchProperties> getBatchConfiguration() {
        return BaseResponse.onSuccess(batchProperties);
    }
}
