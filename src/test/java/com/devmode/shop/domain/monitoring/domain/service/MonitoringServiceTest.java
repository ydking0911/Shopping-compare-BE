package com.devmode.shop.domain.monitoring.domain.service;

import com.devmode.shop.domain.monitoring.application.dto.response.HealthStatusResponse;
import com.devmode.shop.global.config.properties.MonitoringProperties;
import com.devmode.shop.global.config.properties.MonitoringProperties.Alert;
import com.devmode.shop.global.config.properties.MonitoringProperties.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MonitoringService 테스트")
class MonitoringServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private MonitoringProperties monitoringProperties;

    @Mock
    private Alert alert;

    @Mock
    private Cache cache;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private MonitoringService monitoringService;

    @BeforeEach
    void setUp() {
        when(monitoringProperties.getAlert()).thenReturn(alert);
        when(monitoringProperties.getCache()).thenReturn(cache);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // 기본 설정값 설정
        when(alert.getAggregationFailureThreshold()).thenReturn(3);
        when(alert.getCacheHitRateAlertThreshold()).thenReturn(0.6);
        when(alert.getResponseTimeThreshold()).thenReturn(5000L);
        when(cache.getHitRateThreshold()).thenReturn(0.8);
        when(cache.getMaxKeyCount()).thenReturn(10000);
        when(monitoringProperties.isMetricsEnabled()).thenReturn(true);
    }

    @Test
    @DisplayName("요청 메트릭 기록 테스트")
    void testRecordRequest() {
        // when
        monitoringService.recordRequest();
        monitoringService.recordRequest();

        // then
        assertEquals(2L, monitoringService.getRequestCount());
    }

    @Test
    @DisplayName("캐시 히트/미스 메트릭 기록 테스트")
    void testRecordCacheHitAndMiss() {
        // when
        monitoringService.recordCacheHit();
        monitoringService.recordCacheHit();
        monitoringService.recordCacheMiss();

        // then
        assertEquals(2L, monitoringService.getCacheHitCount());
        assertEquals(1L, monitoringService.getCacheMissCount());
    }

    @Test
    @DisplayName("집계 성공/실패 메트릭 기록 테스트")
    void testRecordAggregationSuccessAndFailure() {
        // when
        monitoringService.recordAggregationSuccess();
        monitoringService.recordAggregationSuccess();
        monitoringService.recordAggregationFailure();
        monitoringService.recordAggregationFailure();

        // then
        assertEquals(2L, monitoringService.getAggregationSuccessCount());
        assertEquals(2L, monitoringService.getAggregationFailureCount());
        assertEquals(2, monitoringService.getConsecutiveFailures());
    }

    @Test
    @DisplayName("배치 잡 메트릭 기록 테스트 - 집계 메트릭과 독립적")
    void testRecordBatchJobMetrics_Independent() {
        // when
        monitoringService.recordBatchJobSuccess();
        monitoringService.recordBatchJobSuccess();
        monitoringService.recordBatchJobFailure();

        // then
        assertEquals(2L, monitoringService.getBatchJobSuccessCount());
        assertEquals(1L, monitoringService.getBatchJobFailureCount());
        // 배치 잡은 내부적으로 집계 메트릭도 증가시킴
        assertEquals(2L, monitoringService.getAggregationSuccessCount());
        assertEquals(1L, monitoringService.getAggregationFailureCount());
    }

    @Test
    @DisplayName("연속 실패 후 성공 시 카운터 리셋 테스트")
    void testConsecutiveFailuresReset() {
        // when
        monitoringService.recordAggregationFailure(); // 1
        monitoringService.recordAggregationFailure(); // 2
        monitoringService.recordAggregationSuccess(); // 리셋

        // then
        assertEquals(0, monitoringService.getConsecutiveFailures());
        assertEquals(1L, monitoringService.getAggregationSuccessCount());
    }

    @Test
    @DisplayName("응답 시간 메트릭 기록 테스트")
    void testRecordResponseTime() {
        // when
        monitoringService.recordResponseTime(100L);
        monitoringService.recordResponseTime(200L);
        monitoringService.recordResponseTime(50L);

        // then
        assertEquals(200L, monitoringService.getMaxResponseTime());
    }

    @Test
    @DisplayName("활성 배치 잡 수 관리 테스트")
    void testActiveBatchJobsManagement() {
        // when
        monitoringService.incrementActiveBatchJobs();
        monitoringService.incrementActiveBatchJobs();
        monitoringService.decrementActiveBatchJobs();

        // then
        assertEquals(1, monitoringService.getActiveBatchJobs());
    }

    @Test
    @DisplayName("상세 헬스체크 정보 조회 테스트")
    void testGetDetailedHealthStatus() {
        // when
        monitoringService.recordRequest();
        monitoringService.recordCacheHit();
        monitoringService.recordAggregationSuccess();

        // when
        HealthStatusResponse response = monitoringService.getDetailedHealthStatus();

        // then
        assertNotNull(response);
        assertEquals(1L, response.requestCount());
        assertEquals(1L, response.cacheHitCount());
        assertEquals(0L, response.cacheMissCount());
        assertEquals(1L, response.aggregationSuccessCount());
        assertEquals(0L, response.aggregationFailureCount());
        assertEquals(0, response.consecutiveFailures());
        assertEquals(0.0, response.batchJobSuccessRate(), 0.001); // 배치 잡이 없으므로 0.0
        assertEquals(0L, response.averageBatchJobExecutionTime());
        assertEquals(0, response.activeBatchJobs());
    }

    @Test
    @DisplayName("메트릭 초기화 테스트")
    void testResetDailyMetrics() {
        // given
        monitoringService.recordRequest();
        monitoringService.recordCacheHit();
        monitoringService.recordAggregationSuccess();
        monitoringService.recordBatchJobSuccess();

        // when
        monitoringService.resetDailyMetrics();

        // then
        assertEquals(0L, monitoringService.getRequestCount());
        assertEquals(0L, monitoringService.getCacheHitCount());
        assertEquals(0L, monitoringService.getCacheMissCount());
        assertEquals(0L, monitoringService.getAggregationSuccessCount());
        assertEquals(0L, monitoringService.getAggregationFailureCount());
        assertEquals(0, monitoringService.getConsecutiveFailures());
        assertEquals(0L, monitoringService.getBatchJobSuccessCount());
        assertEquals(0L, monitoringService.getBatchJobFailureCount());
        assertEquals(0, monitoringService.getActiveBatchJobs());
    }
}
