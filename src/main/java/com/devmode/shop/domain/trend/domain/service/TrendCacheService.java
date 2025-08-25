package com.devmode.shop.domain.trend.domain.service;

import com.devmode.shop.domain.trend.application.dto.request.TrendSearchRequest;
import com.devmode.shop.domain.trend.application.dto.response.trend.TrendSearchResponse;
import com.devmode.shop.global.config.properties.DataLabApiProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.devmode.shop.domain.product.domain.repository.SearchHistoryRepository;
import com.devmode.shop.domain.trend.domain.entity.UserInterestKeywords;
import com.devmode.shop.domain.trend.domain.repository.UserInterestKeywordsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class TrendCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final DataLabApiProperties dataLabApiProperties;
    private final ObjectMapper objectMapper;
    private final SearchHistoryRepository searchHistoryRepository;
    private final UserInterestKeywordsRepository userInterestKeywordsRepository;

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
    
    // 사용자 검색 히스토리 관련 메서드들
    private static final String USER_SEARCH_HISTORY_PREFIX = "user_search_history:";
    private static final String USER_INTEREST_KEYWORDS_PREFIX = "user_interest_keywords:";
    
    /**
     * 사용자 검색 히스토리 캐시에서 조회
     */
    public List<String> getUserSearchHistory(String userId) {
        try {
            String key = USER_SEARCH_HISTORY_PREFIX + userId;
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return objectMapper.readValue(cached, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            }
        } catch (Exception e) {
            // 로깅 없이 조용히 실패 처리
        }
        return null;
    }
    
    /**
     * 데이터베이스에서 사용자 검색 히스토리 조회
     */
    public List<String> getUserSearchHistoryFromDatabase(String userId) {
        try {
            return searchHistoryRepository.findRecentKeywordsByUserId(userId);
        } catch (Exception e) {
            // 데이터베이스 오류 시 빈 리스트 반환
            return List.of();
        }
    }
    
    /**
     * 사용자 검색 히스토리 캐시에 저장
     */
    public void cacheUserSearchHistory(String userId, List<String> searchHistory) {
        try {
            String key = USER_SEARCH_HISTORY_PREFIX + userId;
            String jsonHistory = objectMapper.writeValueAsString(searchHistory);
            redisTemplate.opsForValue().set(key, jsonHistory, Duration.ofHours(24)); // 24시간 캐시
        } catch (Exception e) {
            // 로깅 없이 조용히 실패 처리
        }
    }
    
    /**
     * 사용자 관심 키워드 캐시에서 조회
     */
    public List<String> getUserInterestKeywords(String userId) {
        try {
            String key = USER_INTEREST_KEYWORDS_PREFIX + userId;
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return objectMapper.readValue(cached, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            }
        } catch (Exception e) {
            // 로깅 없이 조용히 실패 처리
        }
        return List.of();
    }

    /**
     * 프리페치 시드 키워드 캐싱
     */
    public void cacheTrendKeywords(String cacheKey, List<String> keywords, Duration ttl) {
        try {
            String jsonKeywords = objectMapper.writeValueAsString(keywords);
            redisTemplate.opsForValue().set(cacheKey, jsonKeywords, ttl);
        } catch (JsonProcessingException e) {
            // 로깅 없이 조용히 실패 처리
        }
    }

    /**
     * 캐시된 프리페치 시드 키워드 조회
     */
    public List<String> getCachedTrendKeywords(String cacheKey) {
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.readValue(cached, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            }
        } catch (Exception e) {
            // 로깅 없이 조용히 실패 처리
        }
        return List.of();
    }

    /**
     * 프리페치 트렌드 데이터 캐싱
     */
    public void cacheTrendData(String cacheKey, Object trendData, Duration ttl) {
        try {
            String jsonData = objectMapper.writeValueAsString(trendData);
            redisTemplate.opsForValue().set(cacheKey, jsonData, ttl);
        } catch (JsonProcessingException e) {
            // 로깅 없이 조용히 실패 처리
        }
    }

    /**
     * 캐시된 프리페치 트렌드 데이터 조회
     */
    public <T> T getCachedTrendData(String cacheKey, Class<T> clazz) {
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.readValue(cached, clazz);
            }
        } catch (Exception e) {
            // 로깅 없이 조용히 실패 처리
        }
        return null;
    }

    /**
     * 패턴으로 캐시 무효화
     */
    public void invalidateCacheByPattern(String pattern) {
        try {
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            // 로깅 없이 조용히 실패 처리
        }
    }
    
    /**
     * 사용자 관심 키워드 캐시에 저장
     */
    public void cacheUserInterestKeywords(String userId, List<String> keywords) {
        try {
            String key = USER_INTEREST_KEYWORDS_PREFIX + userId;
            String jsonKeywords = objectMapper.writeValueAsString(keywords);
            redisTemplate.opsForValue().set(key, jsonKeywords, Duration.ofDays(7)); // 7일 캐시
        } catch (Exception e) {
            // 로깅 없이 조용히 실패 처리
        }
    }
    
    /**
     * 사용자 관심 키워드 데이터베이스에 저장
     */
    @Transactional
    public void saveUserInterestKeywords(String userId, List<String> keywords) {
        try {
            // 1. 기존 관심 키워드 비활성화
            userInterestKeywordsRepository.deactivateAllByUserId(userId);
            
            // 2. 새로운 관심 키워드 저장
            AtomicInteger priority = new AtomicInteger(1);
            List<UserInterestKeywords> newKeywords = keywords.stream()
                    .map(keyword -> UserInterestKeywords.builder()
                            .userId(userId)
                            .keyword(keyword)
                            .priority(priority.getAndIncrement())
                            .isActive(true)
                            .lastUpdatedAt(LocalDateTime.now())
                            .build())
                    .toList();
            
            userInterestKeywordsRepository.saveAll(newKeywords);
            
            // 3. 캐시에도 저장
            cacheUserInterestKeywords(userId, keywords);
            
        } catch (Exception e) {
            // 데이터베이스 저장 실패 시 캐시에만 저장
            cacheUserInterestKeywords(userId, keywords);
        }
    }
}
