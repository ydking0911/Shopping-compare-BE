package com.devmode.shop.domain.batch.application.usecase;

import com.devmode.shop.domain.batch.application.dto.request.BatchJobRequest;
import com.devmode.shop.domain.batch.application.dto.response.BatchJobResponse;
import com.devmode.shop.domain.batch.domain.service.BatchJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BatchJobUseCase {

    private final BatchJobService batchJobService;

    /**
     * 프리페치 트렌드 배치 잡 실행
     */
    public BatchJobResponse executePrefetchJob(BatchJobRequest request) {
        try {
            log.info("[Batch] 프리페치 트렌드 잡 실행 시작: {}", request);
            
            var result = batchJobService.executePrefetchJob(request);
            
            log.info("[Batch] 프리페치 트렌드 잡 실행 완료: {}", result);
            return result;
            
        } catch (Exception e) {
            log.error("[Batch] 프리페치 트렌드 잡 실행 실패: {}", e.getMessage(), e);
            throw new RuntimeException("프리페치 트렌드 잡 실행에 실패했습니다.", e);
        }
    }

    /**
     * 일별 집계 배치 잡 실행
     */
    public BatchJobResponse executeDailyAggregationJob(BatchJobRequest request) {
        try {
            log.info("[Batch] 일별 집계 잡 실행 시작: {}", request);
            
            var result = batchJobService.executeDailyAggregationJob(request);
            
            log.info("[Batch] 일별 집계 잡 실행 완료: {}", result);
            return result;
            
        } catch (Exception e) {
            log.error("[Batch] 일별 집계 잡 실행 실패: {}", e.getMessage(), e);
            throw new RuntimeException("일별 집계 잡 실행에 실패했습니다.", e);
        }
    }

    /**
     * 주별 집계 배치 잡 실행
     */
    public BatchJobResponse executeWeeklyAggregationJob(BatchJobRequest request) {
        try {
            log.info("[Batch] 주별 집계 잡 실행 시작: {}", request);
            
            var result = batchJobService.executeWeeklyAggregationJob(request);
            
            log.info("[Batch] 주별 집계 잡 실행 완료: {}", result);
            return result;
            
        } catch (Exception e) {
            log.error("[Batch] 주별 집계 잡 실행 실패: {}", e.getMessage(), e);
            throw new RuntimeException("주별 집계 잡 실행에 실패했습니다.", e);
        }
    }

    /**
     * 월별 집계 배치 잡 실행
     */
    public BatchJobResponse executeMonthlyAggregationJob(BatchJobRequest request) {
        try {
            log.info("[Batch] 월별 집계 잡 실행 시작: {}", request);
            
            var result = batchJobService.executeMonthlyAggregationJob(request);
            
            log.info("[Batch] 월별 집계 잡 실행 완료: {}", result);
            return result;
            
        } catch (Exception e) {
            log.error("[Batch] 월별 집계 잡 실행 실패: {}", e.getMessage(), e);
            throw new RuntimeException("월별 집계 잡 실행에 실패했습니다.", e);
        }
    }
}
