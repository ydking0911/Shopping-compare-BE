package com.devmode.shop.domain.trend.application.usecase;

import com.devmode.shop.domain.trend.domain.service.TrendCacheService;
import com.devmode.shop.domain.trend.domain.service.NaverDataLabApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PrefetchTrendsUseCase {

    private final NaverDataLabApiService dataLabApiService;
    private final TrendCacheService trendCacheService;

    private static final String SEED_CACHE_KEY = "trend:prefetch:seed";
    private static final String PREFETCH_CACHE_KEY = "trend:prefetch:data";
    private static final Duration SEED_CACHE_TTL = Duration.ofDays(1); // 시드 데이터는 1일간 캐시
    private static final Duration PREFETCH_CACHE_TTL = Duration.ofHours(1); // 프리페치 데이터는 1시간간 캐시

    /**
     * 초기 시드 데이터 로드 및 캐싱
     */
    public void loadInitialSeed() {
        try {
            log.info("[Prefetch] 초기 시드 데이터 로드 시작");
            
            // DataLab에서 상위 키워드 가져오기
            List<String> topKeywords = dataLabApiService.fetchTopKeywords();
            
            if (topKeywords != null && !topKeywords.isEmpty()) {
                // Redis에 시드 데이터 캐싱
                trendCacheService.cacheTrendKeywords(SEED_CACHE_KEY, topKeywords, SEED_CACHE_TTL);
                log.info("[Prefetch] 초기 시드 데이터 캐싱 완료: {}개 키워드", topKeywords.size());
            } else {
                log.warn("[Prefetch] DataLab에서 키워드를 가져올 수 없습니다.");
            }
            
        } catch (Exception e) {
            log.error("[Prefetch] 초기 시드 데이터 로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("초기 시드 데이터 로드에 실패했습니다.", e);
        }
    }

    /**
     * 트렌드 데이터 프리페치 및 캐싱
     */
    public void prefetchTrends() {
        try {
            log.info("[Prefetch] 트렌드 데이터 프리페치 시작");
            
            // 캐시된 시드 데이터 가져오기
            List<String> seedKeywords = trendCacheService.getCachedTrendKeywords(SEED_CACHE_KEY);
            
            if (seedKeywords == null || seedKeywords.isEmpty()) {
                log.warn("[Prefetch] 시드 데이터가 없습니다. 초기 시드 데이터를 먼저 로드하세요.");
                return;
            }
            
            // 각 키워드에 대해 트렌드 데이터 프리페치
            for (String keyword : seedKeywords) {
                try {
                    // DataLab에서 키워드별 트렌드 데이터 가져오기
                    var trendData = dataLabApiService.fetchTrendData(keyword);
                    
                    if (trendData != null) {
                        // Redis에 프리페치 데이터 캐싱
                        String cacheKey = PREFETCH_CACHE_KEY + ":" + keyword;
                        trendCacheService.cacheTrendData(cacheKey, trendData, PREFETCH_CACHE_TTL);
                        log.debug("[Prefetch] 키워드 '{}' 프리페치 완료", keyword);
                    }
                    
                } catch (Exception e) {
                    log.warn("[Prefetch] 키워드 '{}' 프리페치 실패: {}", keyword, e.getMessage());
                    // 개별 키워드 실패는 전체 프로세스를 중단하지 않음
                }
            }
            
            log.info("[Prefetch] 트렌드 데이터 프리페치 완료: {}개 키워드", seedKeywords.size());
            
        } catch (Exception e) {
            log.error("[Prefetch] 트렌드 데이터 프리페치 실패: {}", e.getMessage(), e);
            throw new RuntimeException("트렌드 데이터 프리페치에 실패했습니다.", e);
        }
    }

    /**
     * 캐시된 시드 데이터 조회
     */
    public List<String> getCachedSeedKeywords() {
        return trendCacheService.getCachedTrendKeywords(SEED_CACHE_KEY);
    }

    /**
     * 캐시된 프리페치 데이터 조회
     */
    public Object getCachedPrefetchData(String keyword) {
        String cacheKey = PREFETCH_CACHE_KEY + ":" + keyword;
        return trendCacheService.getCachedTrendData(cacheKey, Object.class);
    }

    /**
     * 캐시 무효화
     */
    public void invalidateCache() {
        try {
            trendCacheService.invalidateCacheByPattern("trend:prefetch:*");
            log.info("[Prefetch] 프리페치 캐시 무효화 완료");
        } catch (Exception e) {
            log.error("[Prefetch] 캐시 무효화 실패: {}", e.getMessage(), e);
        }
    }
}


