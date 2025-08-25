package com.devmode.shop.domain.batch.domain.service;

import com.devmode.shop.domain.batch.application.dto.request.BatchJobRequest;
import com.devmode.shop.domain.batch.application.dto.response.BatchJobResponse;
import com.devmode.shop.domain.monitoring.domain.service.MonitoringService;
import com.devmode.shop.global.config.properties.BatchProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobService {

    private final JobLauncher jobLauncher;
    private final Job prefetchTrendsJob;
    private final Job aggregationJob;
    private final BatchProperties batchProperties;
    private final MonitoringService monitoringService;

    /**
     * 프리페치 트렌드 배치 잡 실행
     */
    public BatchJobResponse executePrefetchJob(BatchJobRequest request) {
        String jobId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            log.info("[Batch] 프리페치 트렌드 잡 실행 시작: jobId={}", jobId);
            
            // 활성 배치 잡 수 증가
            monitoringService.incrementActiveBatchJobs();
            
            JobParameters jobParameters = buildJobParameters(request, jobId);
            var execution = jobLauncher.run(prefetchTrendsJob, jobParameters);
            
            LocalDateTime endTime = LocalDateTime.now();
            long executionTime = java.time.Duration.between(startTime, endTime).toMillis();
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("executionId", execution.getId());
            metadata.put("exitCode", execution.getStatus().name());
            metadata.put("duration", executionTime);
            
            // 모니터링 메트릭 기록
            monitoringService.recordBatchJobSuccess();
            monitoringService.recordBatchJobExecutionTime(executionTime);
            
            // 활성 배치 잡 수 감소
            monitoringService.decrementActiveBatchJobs();
            
            return new BatchJobResponse(
                    jobId,
                    request.jobType(),
                    "COMPLETED",
                    startTime,
                    endTime,
                    "프리페치 트렌드 잡이 성공적으로 완료되었습니다.",
                    metadata
            );
            
        } catch (JobExecutionAlreadyRunningException e) {
            log.warn("[Batch] 프리페치 잡이 이미 실행 중입니다: jobId={}", jobId);
            monitoringService.decrementActiveBatchJobs();
            return createFailureResponse(jobId, request, startTime, "프리페치 잡이 이미 실행 중입니다.");
        } catch (JobRestartException e) {
            log.error("[Batch] 프리페치 잡 재시작 실패: jobId={}", jobId, e);
            monitoringService.recordBatchJobFailure();
            monitoringService.decrementActiveBatchJobs();
            return createFailureResponse(jobId, request, startTime, "프리페치 잡 재시작에 실패했습니다: " + e.getMessage());
        } catch (JobInstanceAlreadyCompleteException e) {
            log.warn("[Batch] 프리페치 잡이 이미 완료되었습니다: jobId={}", jobId);
            monitoringService.decrementActiveBatchJobs();
            return createFailureResponse(jobId, request, startTime, "프리페치 잡이 이미 완료되었습니다.");
        } catch (JobParametersInvalidException e) {
            log.error("[Batch] 프리페치 잡 파라미터 오류: jobId={}", jobId, e);
            monitoringService.recordBatchJobFailure();
            monitoringService.decrementActiveBatchJobs();
            return createFailureResponse(jobId, request, startTime, "프리페치 잡 파라미터 오류: " + e.getMessage());
        } catch (Exception e) {
            log.error("[Batch] 프리페치 잡 실행 중 예외 발생: jobId={}", jobId, e);
            monitoringService.recordBatchJobFailure();
            monitoringService.decrementActiveBatchJobs();
            return createFailureResponse(jobId, request, startTime, "프리페치 잡 실행 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 일별 집계 배치 잡 실행
     */
    public BatchJobResponse executeDailyAggregationJob(BatchJobRequest request) {
        String jobId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            log.info("[Batch] 일별 집계 잡 실행 시작: jobId={}", jobId);
            
            JobParameters jobParameters = buildJobParameters(request, jobId);
            var execution = jobLauncher.run(aggregationJob, jobParameters);
            
            LocalDateTime endTime = LocalDateTime.now();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("executionId", execution.getId());
            metadata.put("exitCode", execution.getStatus().name());
            metadata.put("duration", java.time.Duration.between(startTime, endTime).toMillis());
            
            monitoringService.recordAggregationSuccess();
            
            return new BatchJobResponse(
                    jobId,
                    request.jobType(),
                    "COMPLETED",
                    startTime,
                    endTime,
                    "일별 집계 잡이 성공적으로 완료되었습니다.",
                    metadata
            );
            
        } catch (Exception e) {
            log.error("[Batch] 일별 집계 잡 실행 중 예외 발생: jobId={}", jobId, e);
            monitoringService.recordAggregationFailure();
            return createFailureResponse(jobId, request, startTime, "일별 집계 잡 실행 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 주별 집계 배치 잡 실행
     */
    public BatchJobResponse executeWeeklyAggregationJob(BatchJobRequest request) {
        String jobId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            log.info("[Batch] 주별 집계 잡 실행 시작: jobId={}", jobId);
            
            JobParameters jobParameters = buildJobParameters(request, jobId);
            var execution = jobLauncher.run(aggregationJob, jobParameters);
            
            LocalDateTime endTime = LocalDateTime.now();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("executionId", execution.getId());
            metadata.put("exitCode", execution.getStatus().name());
            metadata.put("duration", java.time.Duration.between(startTime, endTime).toMillis());
            
            monitoringService.recordAggregationSuccess();
            
            return new BatchJobResponse(
                    jobId,
                    request.jobType(),
                    "COMPLETED",
                    startTime,
                    endTime,
                    "주별 집계 잡이 성공적으로 완료되었습니다.",
                    metadata
            );
            
        } catch (Exception e) {
            log.error("[Batch] 주별 집계 잡 실행 중 예외 발생: jobId={}", jobId, e);
            monitoringService.recordAggregationFailure();
            return createFailureResponse(jobId, request, startTime, "주별 집계 잡 실행 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 월별 집계 배치 잡 실행
     */
    public BatchJobResponse executeMonthlyAggregationJob(BatchJobRequest request) {
        String jobId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            log.info("[Batch] 월별 집계 잡 실행 시작: jobId={}", jobId);
            
            JobParameters jobParameters = buildJobParameters(request, jobId);
            var execution = jobLauncher.run(aggregationJob, jobParameters);
            
            LocalDateTime endTime = LocalDateTime.now();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("executionId", execution.getId());
            metadata.put("exitCode", execution.getStatus().name());
            metadata.put("duration", java.time.Duration.between(startTime, endTime).toMillis());
            
            monitoringService.recordAggregationSuccess();
            
            return new BatchJobResponse(
                    jobId,
                    request.jobType(),
                    "COMPLETED",
                    startTime,
                    endTime,
                    "월별 집계 잡이 성공적으로 완료되었습니다.",
                    metadata
            );
            
        } catch (Exception e) {
            log.error("[Batch] 월별 집계 잡 실행 중 예외 발생: jobId={}", jobId, e);
            monitoringService.recordAggregationFailure();
            return createFailureResponse(jobId, request, startTime, "월별 집계 잡 실행 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 잡 파라미터 생성
     */
    private JobParameters buildJobParameters(BatchJobRequest request, String jobId) {
        JobParametersBuilder builder = new JobParametersBuilder()
                .addString("jobId", jobId)
                .addString("executionTime", request.scheduledTime().toString())
                .addLong("timestamp", System.currentTimeMillis())
                .addString("jobType", request.jobType())
                .addString("triggeredBy", request.triggeredBy());

        if (request.parameters() != null) {
            request.parameters().forEach(builder::addString);
        }

        return builder.toJobParameters();
    }

    /**
     * 실패 응답 생성
     */
    private BatchJobResponse createFailureResponse(String jobId, BatchJobRequest request, LocalDateTime startTime, String message) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("error", message);
        metadata.put("duration", 0L);
        
        return new BatchJobResponse(
                jobId,
                request.jobType(),
                "FAILED",
                startTime,
                LocalDateTime.now(),
                message,
                metadata
        );
    }
}
