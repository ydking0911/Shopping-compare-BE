package com.devmode.shop.domain.trend.domain.service;

import com.devmode.shop.domain.trend.application.dto.request.TrendSearchRequest;
import com.devmode.shop.domain.trend.application.dto.response.trend.TrendSearchResponse;
import com.devmode.shop.global.config.properties.DataLabApiProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrendCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final DataLabApiProperties dataLabApiProperties;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "trend:";
    private static final String SEARCH_PREFIX = "trend_search:";

    public void cacheTrendData(String keyword, LocalDate date, String data) {
        String key = CACHE_PREFIX + keyword + ":" + date;
        redisTemplate.opsForValue().set(key, data, Duration.ofSeconds(dataLabApiProperties.getCacheTtl()));
    }

    public Optional<String> getCachedTrendData(String keyword, LocalDate date) {
        String key = CACHE_PREFIX + keyword + ":" + date;
        String cached = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(cached);
    }

    public void cacheSearchResult(TrendSearchRequest request, TrendSearchResponse response) {
        try {
            String cacheKey = buildCacheKey(request);
            String jsonResponse = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, jsonResponse, Duration.ofSeconds(dataLabApiProperties.getCacheTtl()));
        } catch (JsonProcessingException e) {
            // 로깅 없이 조용히 실패 처리
        }
    }

    public Optional<TrendSearchResponse> getCachedSearchResult(TrendSearchRequest request) {
        try {
            String cacheKey = buildCacheKey(request);
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return Optional.of(objectMapper.readValue(cached, TrendSearchResponse.class));
            }
        } catch (Exception e) {
            // 로깅 없이 조용히 실패 처리
        }
        return Optional.empty();
    }

    public boolean isCached(TrendSearchRequest request) {
        String cacheKey = buildCacheKey(request);
        return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
    }

    private String buildCacheKey(TrendSearchRequest request) {
        return SEARCH_PREFIX + 
               request.keyword() + ":" + 
               request.startDate() + ":" + 
               request.endDate() + ":" + 
               (request.timeUnit() != null ? request.timeUnit() : "date");
    }

    public void clearCache(String keyword) {
        String pattern = CACHE_PREFIX + keyword + "*";
        redisTemplate.delete(redisTemplate.keys(pattern));
    }

    public void clearSearchCache(String keyword) {
        String pattern = SEARCH_PREFIX + keyword + "*";
        redisTemplate.delete(redisTemplate.keys(pattern));
    }
}
