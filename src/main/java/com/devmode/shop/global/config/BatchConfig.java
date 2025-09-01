package com.devmode.shop.global.config;

import com.devmode.shop.domain.batch.application.usecase.BatchJobUseCase;
import com.devmode.shop.domain.batch.application.dto.request.BatchJobRequest;
import com.devmode.shop.domain.trend.application.usecase.PrefetchTrendsUseCase;
import com.devmode.shop.domain.trend.application.usecase.TrendAggregationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final PrefetchTrendsUseCase prefetchTrendsUseCase;
    private final TrendAggregationUseCase trendAggregationUseCase;

    /**
     * 프리페치 트렌드 배치 잡
     */
    @Bean
    public Job prefetchTrendsJob() {
        return new JobBuilder("prefetchTrendsJob", jobRepository)
                .start(prefetchTrendsStep())
                .on("FAILED")
                .to(prefetchTrendsFailureStep())
                .on("*")
                .end()
                .end()
                .build();
    }

    /**
     * 집계 배치 잡
     */
    @Bean
    public Job aggregationJob() {
        return new JobBuilder("aggregationJob", jobRepository)
                .start(dailyAggregationStep())
                .next(weeklyAggregationStep())
                .next(monthlyAggregationStep())
                .on("FAILED")
                .to(aggregationFailureStep())
                .on("*")
                .end()
                .end()
                .build();
    }

    /**
     * 프리페치 트렌드 스텝
     */
    @Bean
    public Step prefetchTrendsStep() {
        return new StepBuilder("prefetchTrendsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("[Batch] 프리페치 트렌드 작업 시작");
                    
                    try {
                        // 실제 프리페치 작업 실행
                        prefetchTrendsUseCase.prefetchTrends();
                        log.info("[Batch] 프리페치 트렌드 작업 완료");
                        return RepeatStatus.FINISHED;
                    } catch (Exception e) {
                        log.error("[Batch] 프리페치 트렌드 작업 실패: {}", e.getMessage(), e);
                        contribution.setExitStatus(org.springframework.batch.core.ExitStatus.FAILED);
                        return RepeatStatus.FINISHED;
                    }
                }, transactionManager)
                .build();
    }

    /**
     * 프리페치 실패 처리 스텝
     */
    @Bean
    public Step prefetchTrendsFailureStep() {
        return new StepBuilder("prefetchTrendsFailureStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.error("[Batch] 프리페치 트렌드 작업 실패 처리");
                    
                    // 실패 알림, 로깅, 복구 로직
                    try {
                        // TODO: 실패 알림 발송 (Slack, Email 등)
                        log.error("[Batch] 프리페치 실패 알림 발송 필요");
                        
                        // TODO: 복구 로직 (재시도 또는 수동 개입 필요)
                        log.error("[Batch] 프리페치 복구 로직 필요");
                        
                    } catch (Exception e) {
                        log.error("[Batch] 실패 처리 중 오류 발생: {}", e.getMessage(), e);
                    }
                    
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    /**
     * 일별 집계 스텝
     */
    @Bean
    public Step dailyAggregationStep() {
        return new StepBuilder("dailyAggregationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("[Batch] 일별 집계 작업 시작");
                    
                    try {
                        // 실제 일별 집계 작업 실행
                        List<String> defaultKeywords = List.of("아이폰", "갤럭시", "맥북", "에어팟", "노트북");
                        trendAggregationUseCase.aggregateDaily(defaultKeywords);
                        log.info("[Batch] 일별 집계 작업 완료");
                        return RepeatStatus.FINISHED;
                    } catch (Exception e) {
                        log.error("[Batch] 일별 집계 작업 실패: {}", e.getMessage(), e);
                        contribution.setExitStatus(org.springframework.batch.core.ExitStatus.FAILED);
                        return RepeatStatus.FINISHED;
                    }
                }, transactionManager)
                .build();
    }

    /**
     * 주별 집계 스텝
     */
    @Bean
    public Step weeklyAggregationStep() {
        return new StepBuilder("weeklyAggregationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("[Batch] 주별 집계 작업 시작");
                    
                    try {
                        // 실제 주별 집계 작업 실행
                        List<String> defaultKeywords = List.of("아이폰", "갤럭시", "맥북", "에어팟", "노트북");
                        trendAggregationUseCase.aggregateWeekly(defaultKeywords);
                        log.info("[Batch] 주별 집계 작업 완료");
                        return RepeatStatus.FINISHED;
                    } catch (Exception e) {
                        log.error("[Batch] 주별 집계 작업 실패: {}", e.getMessage(), e);
                        contribution.setExitStatus(org.springframework.batch.core.ExitStatus.FAILED);
                        return RepeatStatus.FINISHED;
                    }
                }, transactionManager)
                .build();
    }

    /**
     * 월별 집계 스텝
     */
    @Bean
    public Step monthlyAggregationStep() {
        return new StepBuilder("monthlyAggregationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("[Batch] 월별 집계 작업 시작");
                    
                    try {
                        // 실제 월별 집계 작업 실행
                        List<String> defaultKeywords = List.of("아이폰", "갤럭시", "맥북", "에어팟", "노트북");
                        trendAggregationUseCase.aggregateMonthly(defaultKeywords);
                        log.info("[Batch] 월별 집계 작업 완료");
                        return RepeatStatus.FINISHED;
                    } catch (Exception e) {
                        log.error("[Batch] 월별 집계 작업 실패: {}", e.getMessage(), e);
                        contribution.setExitStatus(org.springframework.batch.core.ExitStatus.FAILED);
                        return RepeatStatus.FINISHED;
                    }
                }, transactionManager)
                .build();
    }

    /**
     * 집계 실패 처리 스텝
     */
    @Bean
    public Step aggregationFailureStep() {
        return new StepBuilder("aggregationFailureStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.error("[Batch] 집계 작업 실패 처리");
                    
                    // 실패 알림, 로깅, 복구 로직
                    try {
                        // TODO: 실패 알림 발송 (Slack, Email 등)
                        log.error("[Batch] 집계 실패 알림 발송 필요");
                        
                        // TODO: 복구 로직 (재시도 또는 수동 개입 필요)
                        log.error("[Batch] 집계 복구 로직 필요");
                        
                    } catch (Exception e) {
                        log.error("[Batch] 실패 처리 중 오류 발생: {}", e.getMessage(), e);
                    }
                    
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    /**
     * 배치 재시도를 위한 ScheduledExecutorService
     */
    @Bean
    public ScheduledExecutorService batchRetryExecutor() {
        return Executors.newScheduledThreadPool(
            5, // 코어 스레드 수
            r -> {
                Thread t = new Thread(r, "batch-retry-" + System.currentTimeMillis());
                t.setDaemon(true); // 데몬 스레드로 설정하여 애플리케이션 종료 시 자동 정리
                return t;
            }
        );
    }
}
