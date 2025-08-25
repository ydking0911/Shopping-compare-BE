package com.devmode.shop.global.swagger;

import com.devmode.shop.global.config.properties.BatchProperties;
import com.devmode.shop.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Batch Jobs")
@RequestMapping("/api/batch")
public interface BatchJobApi {

    @Operation(summary = "프리페치 트렌드 잡 수동 실행")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PostMapping("/prefetch")
    BaseResponse<String> triggerPrefetchJob();

    @Operation(summary = "일별 집계 잡 수동 실행")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PostMapping("/aggregation/daily")
    BaseResponse<String> triggerDailyAggregationJob();

    @Operation(summary = "주별 집계 잡 수동 실행")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PostMapping("/aggregation/weekly")
    BaseResponse<String> triggerWeeklyAggregationJob();

    @Operation(summary = "월별 집계 잡 수동 실행")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PostMapping("/aggregation/monthly")
    BaseResponse<String> triggerMonthlyAggregationJob();

    @Operation(summary = "배치 설정 조회")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BatchProperties.class)))
    @PostMapping("/config")
    BaseResponse<BatchProperties> getBatchConfiguration();
}
