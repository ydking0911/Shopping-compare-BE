package com.devmode.shop.global.swagger;

import com.devmode.shop.domain.monitoring.application.dto.response.HealthStatusResponse;
import com.devmode.shop.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Monitoring")
@RequestMapping("/api/monitoring")
public interface MonitoringApi {

    @Operation(summary = "헬스체크")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping("/health")
    BaseResponse<Boolean> healthCheck();

    @Operation(summary = "상세 헬스체크")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = HealthStatusResponse.class)))
    @GetMapping("/health/detailed")
    BaseResponse<HealthStatusResponse> detailedHealthCheck();

    @Operation(summary = "메트릭 통계")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping("/metrics")
    BaseResponse<Void> getMetrics();

    @Operation(summary = "캐시 상태")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping("/cache/status")
    BaseResponse<Void> getCacheStatus();
}
