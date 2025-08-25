package com.devmode.shop.domain.monitoring.ui;

import com.devmode.shop.domain.monitoring.application.dto.response.HealthStatusResponse;
import com.devmode.shop.domain.monitoring.domain.service.MonitoringService;
import com.devmode.shop.global.common.BaseResponse;
import com.devmode.shop.global.swagger.MonitoringApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MonitoringController implements MonitoringApi {

    private final MonitoringService monitoringService;

    @Override
    public BaseResponse<Boolean> healthCheck() {
        boolean isHealthy = monitoringService.isHealthy();
        return BaseResponse.onSuccess(isHealthy);
    }

    @Override
    public BaseResponse<HealthStatusResponse> detailedHealthCheck() {
        var healthStatus = monitoringService.getDetailedHealthStatus();
        return BaseResponse.onSuccess(healthStatus);
    }

    @Override
    public BaseResponse<Void> getMetrics() {
        // 메트릭은 이미 스케줄러에서 자동 로깅되므로 여기서는 추가 작업 없음
        return BaseResponse.onSuccess();
    }

    @Override
    public BaseResponse<Void> getCacheStatus() {
        // 캐시 상태는 이미 스케줄러에서 자동 모니터링되므로 여기서는 추가 작업 없음
        return BaseResponse.onSuccess();
    }
}
