package com.devmode.shop.global.swagger;

import com.devmode.shop.domain.trend.application.dto.request.TrendAggregationRequest;
import com.devmode.shop.domain.trend.application.dto.response.TrendAggregationListResponse;
import com.devmode.shop.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Trend Aggregation")
@RequestMapping("/api/trends/aggregation")
public interface TrendAggregationApi {

    @Operation(summary = "일(日) 집계 수행")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PostMapping("/daily")
    BaseResponse<Void> aggregateDaily();

    @Operation(summary = "주(週) 집계 수행")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PostMapping("/weekly")
    BaseResponse<Void> aggregateWeekly();

    @Operation(summary = "월(月) 집계 수행")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PostMapping("/monthly")
    BaseResponse<Void> aggregateMonthly();

    @Operation(summary = "집계 데이터 조회")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TrendAggregationListResponse.class)))
    @PostMapping("/query")
    BaseResponse<TrendAggregationListResponse> queryAggregations(@RequestBody TrendAggregationRequest request);

    @Operation(summary = "캐시 통계 조회")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping("/cache/stats")
    BaseResponse<Void> getCacheStats();

    @Operation(summary = "키워드별 캐시 무효화")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @DeleteMapping("/cache/keyword/{keyword}")
    BaseResponse<Void> invalidateCacheByKeyword(@PathVariable String keyword);

    @Operation(summary = "타입별 캐시 무효화")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @DeleteMapping("/cache/type/{type}")
    BaseResponse<Void> invalidateCacheByType(@PathVariable String type);

    @Operation(summary = "전체 캐시 무효화")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @DeleteMapping("/cache/all")
    BaseResponse<Void> invalidateAllCache();
}


