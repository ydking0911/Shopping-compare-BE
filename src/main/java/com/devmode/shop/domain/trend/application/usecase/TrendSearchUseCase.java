package com.devmode.shop.domain.trend.application.usecase;

import com.devmode.shop.domain.trend.application.dto.request.TrendSearchRequest;
import com.devmode.shop.domain.trend.application.dto.response.datalab.NaverDataLabResponse;
import com.devmode.shop.domain.trend.application.dto.response.trend.TrendSearchResponse;
import com.devmode.shop.domain.trend.domain.service.NaverDataLabApiService;
import com.devmode.shop.domain.trend.domain.service.TrendCacheService;
import com.devmode.shop.domain.trend.domain.service.TrendTransformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TrendSearchUseCase {

    private final NaverDataLabApiService naverDataLabApiService;
    private final TrendCacheService trendCacheService;
    private final TrendTransformService transformService;

    public TrendSearchResponse searchTrends(TrendSearchRequest request) {
        long startTime = System.currentTimeMillis();

        // 1. 캐시 확인
        if (trendCacheService.isCached(request)) {
            return trendCacheService.getCachedSearchResult(request)
                    .orElseThrow(() -> new RuntimeException("Failed to retrieve cached result"));
        }

        try {
            // 2. 네이버 DataLab API 호출
            NaverDataLabResponse naverResponse = naverDataLabApiService.searchTrends(request);

            // 3. 응답 변환
            TrendSearchResponse response = transformService.transformToTrendSearchResponse(
                    naverResponse,
                    request,
                    "fresh",
                    System.currentTimeMillis() - startTime,
                    1L, // API 호출 횟수
                    "available" // 쿼터 상태
            );

            // 4. 캐시 저장
            trendCacheService.cacheSearchResult(request, response);

            return response;

        } catch (Exception e) {
            // 5. 캐시 폴백 시도
            try {
                return trendCacheService.getCachedSearchResult(request)
                        .orElseThrow(() -> new RuntimeException("No cached result available for fallback"));
            } catch (Exception fallbackException) {
                throw new RuntimeException("Trend search failed and no fallback available", e);
            }
        }
    }

    public TrendSearchResponse searchTrendsWithCache(TrendSearchRequest request) {
        // 캐시 우선 검색
        return trendCacheService.getCachedSearchResult(request)
                .orElseGet(() -> searchTrends(request));
    }

    public void clearTrendCache(String keyword) {
        trendCacheService.clearCache(keyword);
        trendCacheService.clearSearchCache(keyword);
    }
    
    /**
     * 사용자 검색 히스토리 조회
     */
    public List<String> getUserSearchHistory(String userId) {
        log.info("사용자 검색 히스토리 조회: userId={}", userId);
        
        try {
            // 1. 캐시에서 사용자 검색 히스토리 확인
            List<String> cachedHistory = trendCacheService.getUserSearchHistory(userId);
            if (cachedHistory != null && !cachedHistory.isEmpty()) {
                log.info("사용자 검색 히스토리 캐시에서 조회: userId={}, 히스토리 수={}", userId, cachedHistory.size());
                return cachedHistory;
            }
            
            // 2. 데이터베이스에서 사용자 검색 히스토리 조회
            List<String> searchHistory = trendCacheService.getUserSearchHistoryFromDatabase(userId);
            
            // 3. 캐시에 저장
            if (searchHistory != null && !searchHistory.isEmpty()) {
                trendCacheService.cacheUserSearchHistory(userId, searchHistory);
                log.info("사용자 검색 히스토리 캐시 저장: userId={}, 히스토리 수={}", userId, searchHistory.size());
            }
            
            return searchHistory != null ? searchHistory : List.of();
            
        } catch (Exception e) {
            log.error("사용자 검색 히스토리 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * 사용자 관심 키워드 설정
     */
    public void setUserInterestKeywords(String userId, List<String> keywords) {
        log.info("사용자 관심 키워드 설정: userId={}, keywords={}", userId, keywords);
        
        try {
            // 1. 입력 검증
            if (keywords == null || keywords.isEmpty()) {
                log.warn("사용자 관심 키워드가 비어있음: userId={}", userId);
                return;
            }
            
            // 2. 키워드 정규화 및 필터링
            List<String> normalizedKeywords = keywords.stream()
                .map(String::trim)
                .filter(keyword -> !keyword.isEmpty() && keyword.length() <= 50)
                .distinct()
                .limit(20) // 최대 20개 키워드로 제한
                .collect(Collectors.toList());
            
            // 3. 데이터베이스에 저장
            trendCacheService.saveUserInterestKeywords(userId, normalizedKeywords);
            
            // 4. 캐시 업데이트
            trendCacheService.cacheUserInterestKeywords(userId, normalizedKeywords);
            
            log.info("사용자 관심 키워드 설정 완료: userId={}, 키워드 수={}", userId, normalizedKeywords.size());
            
        } catch (Exception e) {
            log.error("사용자 관심 키워드 설정 실패: userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("관심 키워드 설정에 실패했습니다.", e);
        }
    }
    
    /**
     * 사용자 관심 키워드 조회
     */
    public List<String> getUserInterestKeywords(String userId) {
        log.info("사용자 관심 키워드 조회: userId={}", userId);
        
        try {
            // 1. 캐시에서 사용자 관심 키워드 확인
            List<String> cachedKeywords = trendCacheService.getUserInterestKeywords(userId);
            if (cachedKeywords != null && !cachedKeywords.isEmpty()) {
                log.info("사용자 관심 키워드 캐시에서 조회: userId={}, 키워드 수={}", userId, cachedKeywords.size());
                return cachedKeywords;
            }
            
            // 2. 데이터베이스에서 사용자 관심 키워드 조회
            List<String> interestKeywords = trendCacheService.getUserInterestKeywordsFromDatabase(userId);
            
            // 3. 캐시에 저장
            if (interestKeywords != null && !interestKeywords.isEmpty()) {
                trendCacheService.cacheUserInterestKeywords(userId, interestKeywords);
                log.info("사용자 관심 키워드 캐시 저장: userId={}, 키워드 수={}", userId, interestKeywords.size());
            }
            
            return interestKeywords != null ? interestKeywords : List.of();
            
        } catch (Exception e) {
            log.error("사용자 관심 키워드 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            return List.of();
        }
    }
}
