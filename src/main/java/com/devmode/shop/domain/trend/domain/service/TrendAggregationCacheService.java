package com.devmode.shop.domain.trend.domain.service;

import com.devmode.shop.domain.monitoring.domain.service.MonitoringService;
import com.devmode.shop.domain.trend.application.dto.request.TrendAggregationRequest;
import com.devmode.shop.domain.trend.application.dto.response.TrendAggregationListResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrendAggregationCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final MonitoringService monitoringService;

    private static final String CACHE_PREFIX = "trend:agg:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(1); // 1시간
    private static final Duration DAILY_TTL = Duration.ofHours(6);   // 6시간
    private static final Duration WEEKLY_TTL = Duration.ofHours(12); // 12시간
    private static final Duration MONTHLY_TTL = Duration.ofDays(1);  // 1일

    /**
     * 집계 데이터를 캐시에서 조회
     */
    public Optional<TrendAggregationListResponse> getCachedAggregation(TrendAggregationRequest request) {
        try {
            String cacheKey = buildCacheKey(request);
            String cachedData = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedData != null) {
                TrendAggregationListResponse response = objectMapper.readValue(cachedData, TrendAggregationListResponse.class);
                log.debug("[Cache] 집계 데이터 캐시 히트: key={}", cacheKey);
                monitoringService.recordCacheHit();
                return Optional.of(response);
            }
            
            log.debug("[Cache] 집계 데이터 캐시 미스: key={}", cacheKey);
            monitoringService.recordCacheMiss();
            return Optional.empty();
            
        } catch (Exception e) {
            log.warn("[Cache] 집계 데이터 캐시 조회 실패: request={}, error={}", request, e.getMessage());
            monitoringService.recordCacheMiss();
            return Optional.empty();
        }
    }

    /**
     * 집계 데이터를 캐시에 저장
     */
    public void cacheAggregation(TrendAggregationRequest request, TrendAggregationListResponse response) {
        try {
            String cacheKey = buildCacheKey(request);
            String jsonData = objectMapper.writeValueAsString(response);
            
            Duration ttl = getTtlByType(request.aggregationType());
            redisTemplate.opsForValue().set(cacheKey, jsonData, ttl);
            
            log.debug("[Cache] 집계 데이터 캐시 저장: key={}, ttl={}", cacheKey, ttl);
            
        } catch (Exception e) {
            log.warn("[Cache] 집계 데이터 캐시 저장 실패: request={}, error={}", request, e.getMessage());
        }
    }

    /**
     * 특정 키워드의 집계 데이터 캐시 무효화
     */
    public void invalidateCacheByKeyword(String keyword) {
        try {
            String pattern = CACHE_PREFIX + "*" + keyword + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            log.info("[Cache] 키워드별 캐시 무효화: keyword={}", keyword);
        } catch (Exception e) {
            log.warn("[Cache] 키워드별 캐시 무효화 실패: keyword={}, error={}", keyword, e.getMessage());
        }
    }

    /**
     * 특정 집계 타입의 캐시 무효화
     */
    public void invalidateCacheByType(String aggregationType) {
        try {
            String pattern = CACHE_PREFIX + aggregationType + ":*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            log.info("[Cache] 타입별 캐시 무효화: type={}", aggregationType);
        } catch (Exception e) {
            log.warn("[Cache] 타입별 캐시 무효화 실패: type={}, error={}", aggregationType, e.getMessage());
        }
    }

    /**
     * 전체 집계 캐시 무효화
     */
    public void invalidateAllCache() {
        try {
            String pattern = CACHE_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            log.info("[Cache] 전체 집계 캐시 무효화 완료");
        } catch (Exception e) {
            log.warn("[Cache] 전체 집계 캐시 무효화 실패: error={}", e.getMessage());
        }
    }

    /**
     * 캐시 키 생성
     */
    private String buildCacheKey(TrendAggregationRequest request) {
        StringBuilder keyBuilder = new StringBuilder(CACHE_PREFIX);
        keyBuilder.append(request.aggregationType()).append(":");
        
        // 키워드 정렬하여 일관된 키 생성
        if (request.keywords() != null && !request.keywords().isEmpty()) {
            keyBuilder.append(String.join(",", request.keywords().stream().sorted().toList()));
        } else {
            keyBuilder.append("all");
        }
        keyBuilder.append(":");
        
        // 날짜 범위
        if (request.startDate() != null) {
            keyBuilder.append(request.startDate());
        }
        keyBuilder.append("-");
        if (request.endDate() != null) {
            keyBuilder.append(request.endDate());
        }
        keyBuilder.append(":");
        
        // 정렬 기준
        if (request.sortBy() != null && !request.sortBy().isEmpty()) {
            keyBuilder.append(String.join(",", request.sortBy()));
        }
        keyBuilder.append(":");
        
        // 정렬 순서
        keyBuilder.append(request.sortOrder());
        keyBuilder.append(":");
        
        // 페이징
        keyBuilder.append(request.page()).append(":").append(request.size());
        
        return keyBuilder.toString();
    }

    /**
     * 집계 타입별 TTL 반환
     */
    private Duration getTtlByType(String aggregationType) {
        return switch (aggregationType.toLowerCase()) {
            case "daily" -> DAILY_TTL;
            case "weekly" -> WEEKLY_TTL;
            case "monthly" -> MONTHLY_TTL;
            default -> DEFAULT_TTL;
        };
    }

    /**
     * 캐시 통계 정보 조회
     */
    public void logCacheStats() {
        try {
            String pattern = CACHE_PREFIX + "*";
            var keys = redisTemplate.keys(pattern);
            if (keys != null) {
                log.info("[Cache] 집계 캐시 통계: 총 {}개 키", keys.size());
                
                // 타입별 키 개수
                long dailyCount = keys.stream().filter(key -> key.contains(":daily:")).count();
                long weeklyCount = keys.stream().filter(key -> key.contains(":weekly:")).count();
                long monthlyCount = keys.stream().filter(key -> key.contains(":monthly:")).count();
                
                log.info("[Cache] 타입별 캐시 키 수: daily={}, weekly={}, monthly={}", 
                        dailyCount, weeklyCount, monthlyCount);
            }
        } catch (Exception e) {
            log.warn("[Cache] 캐시 통계 조회 실패: error={}", e.getMessage());
        }
    }
}
