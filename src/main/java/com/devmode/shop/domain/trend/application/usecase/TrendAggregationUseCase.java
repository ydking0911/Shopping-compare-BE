package com.devmode.shop.domain.trend.application.usecase;

import com.devmode.shop.domain.monitoring.domain.service.MonitoringService;
import com.devmode.shop.domain.trend.application.dto.request.TrendAggregationRequest;
import com.devmode.shop.domain.trend.application.dto.response.TrendAggregationListResponse;
import com.devmode.shop.domain.trend.application.dto.response.TrendAggregationResponse;
import com.devmode.shop.domain.trend.domain.repository.TrendAggregationRepository;
import com.devmode.shop.domain.trend.domain.service.TrendAggregationCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TrendAggregationUseCase {

    private final TrendAggregationRepository aggregationRepository;
    private final TrendAggregationCacheService cacheService;
    private final MonitoringService monitoringService;

    /**
     * 키워드 리스트에 대해 일(日) 집계를 수행하여 RDB에 저장
     */
    public void aggregateDaily(List<String> keywords) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);

        for (String keyword : keywords) {
            try {
                // 집계 완료 후 관련 캐시 무효화
                aggregationRepository.upsertDaily(
                        keyword,
                        end,
                        "0.0", // 임시 값 - 실제로는 TrendSearch API 호출 필요
                        "0.0",
                        "0.0",
                        0L,
                        "stable",
                        "0.0"
                );
                
                // 키워드별 캐시 무효화
                cacheService.invalidateCacheByKeyword(keyword);
                monitoringService.recordAggregationSuccess();
                log.info("[Aggregation] 일 집계 완료: keyword={}", keyword);
                
            } catch (Exception e) {
                log.warn("[Aggregation] 일 집계 실패: keyword={}, error={}", keyword, e.getMessage());
                monitoringService.recordAggregationFailure();
            }
        }
    }

    /**
     * 키워드 리스트에 대해 주(週) 집계를 수행하여 RDB에 저장
     */
    public void aggregateWeekly(List<String> keywords) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusWeeks(4);
        String yearWeek = end.format(DateTimeFormatter.ofPattern("yyyy-'W'ww"));

        for (String keyword : keywords) {
            try {
                aggregationRepository.upsertWeekly(
                        keyword,
                        yearWeek,
                        "0.0", // 임시 값
                        "0.0",
                        "0.0",
                        0L,
                        "stable",
                        "0.0"
                );
                
                cacheService.invalidateCacheByKeyword(keyword);
                monitoringService.recordAggregationSuccess();
                log.info("[Aggregation] 주 집계 완료: keyword={}", keyword);
                
            } catch (Exception e) {
                log.warn("[Aggregation] 주 집계 실패: keyword={}, error={}", keyword, e.getMessage());
                monitoringService.recordAggregationFailure();
            }
        }
    }

    /**
     * 키워드 리스트에 대해 월(月) 집계를 수행하여 RDB에 저장
     */
    public void aggregateMonthly(List<String> keywords) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusMonths(3);
        String yearMonth = end.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        for (String keyword : keywords) {
            try {
                aggregationRepository.upsertMonthly(
                        keyword,
                        yearMonth,
                        "0.0", // 임시 값
                        "0.0",
                        0L,
                        "stable",
                        "0.0"
                );
                
                cacheService.invalidateCacheByKeyword(keyword);
                monitoringService.recordAggregationSuccess();
                log.info("[Aggregation] 월 집계 완료: keyword={}", keyword);
                
            } catch (Exception e) {
                log.warn("[Aggregation] 월 집계 실패: keyword={}, error={}", keyword, e.getMessage());
                monitoringService.recordAggregationFailure();
            }
        }
    }

    /**
     * 집계 데이터 조회 (캐시 우선, DB 폴백)
     */
    public TrendAggregationListResponse queryAggregations(TrendAggregationRequest request) {
        long startTime = System.currentTimeMillis();
        monitoringService.recordRequest();
        
        try {
            // 1. 캐시에서 먼저 조회
            var cachedResult = cacheService.getCachedAggregation(request);
            if (cachedResult.isPresent()) {
                log.debug("[Aggregation] 캐시에서 집계 데이터 조회: type={}", request.aggregationType());
                return cachedResult.get();
            }

            // 2. 캐시 미스 시 DB에서 조회
            log.debug("[Aggregation] DB에서 집계 데이터 조회: type={}", request.aggregationType());
            List<TrendAggregationResponse> aggregations;
            int totalElements = 0;
            int offset = request.page() * request.size();

            // 정렬 기준 안전성 검사 추가
            if (request.sortBy() == null || request.sortBy().isEmpty()) {
                throw new IllegalArgumentException("정렬 기준이 비어있습니다.");
            }
            
            String sortBy = request.sortBy().get(0);
            
            switch (request.aggregationType().toLowerCase()) {
                case "daily":
                    aggregations = aggregationRepository.findDailyAggregations(
                            request.keywords(), request.startDate(), request.endDate(),
                            sortBy, request.sortOrder(), offset, request.size()
                    );
                    totalElements = aggregationRepository.countDailyAggregations(
                            request.keywords(), request.startDate(), request.endDate()
                    );
                    break;
                case "weekly":
                    aggregations = aggregationRepository.findWeeklyAggregations(
                            request.keywords(), null, null,
                            sortBy, request.sortOrder(), offset, request.size()
                    );
                    totalElements = aggregationRepository.countWeeklyAggregations(
                            request.keywords(), null, null
                    );
                    break;
                case "monthly":
                    aggregations = aggregationRepository.findMonthlyAggregations(
                            request.keywords(), null, null,
                            sortBy, request.sortOrder(), offset, request.size()
                    );
                    totalElements = aggregationRepository.countMonthlyAggregations(
                            request.keywords(), null, null
                    );
                    break;
                default:
                    throw new IllegalArgumentException("지원하지 않는 집계 타입: " + request.aggregationType());
            }

            int totalPages = (int) Math.ceil((double) totalElements / request.size());
            boolean hasNext = request.page() < totalPages - 1;
            boolean hasPrevious = request.page() > 0;

            TrendAggregationListResponse response = new TrendAggregationListResponse(
                    aggregations, totalElements, totalPages, request.page(), request.size(), hasNext, hasPrevious
            );

            // 3. 조회 결과를 캐시에 저장
            cacheService.cacheAggregation(request, response);
            
            return response;

        } catch (Exception e) {
            log.error("[Aggregation] 집계 조회 실패: request={}, error={}", request, e.getMessage(), e);
            throw new RuntimeException("집계 데이터 조회에 실패했습니다.", e);
        } finally {
            // 응답 시간 기록
            long responseTime = System.currentTimeMillis() - startTime;
            monitoringService.recordResponseTime(responseTime);
        }
    }

    /**
     * 캐시 통계 로깅
     */
    public void logCacheStatistics() {
        cacheService.logCacheStats();
    }
}


