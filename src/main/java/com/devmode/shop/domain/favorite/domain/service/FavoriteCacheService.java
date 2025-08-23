package com.devmode.shop.domain.favorite.domain.service;

import com.devmode.shop.domain.favorite.application.dto.response.FavoriteListResponse;
import com.devmode.shop.domain.favorite.application.dto.request.FavoriteSearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String FAVORITE_CACHE_KEY = "favorite:user:";
    private static final String FAVORITE_STATS_CACHE_KEY = "favorite:stats:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);
    
    /**
     * 사용자의 즐겨찾기 목록 캐싱
     */
    public void cacheUserFavorites(String userId, FavoriteSearchRequest request, FavoriteListResponse favorites) {
        String key = buildCacheKey(userId, request);
        try {
            redisTemplate.opsForValue().set(key, favorites, CACHE_TTL);
            log.debug("즐겨찾기 캐시 저장: key={}", key);
        } catch (Exception e) {
            log.warn("즐겨찾기 캐시 저장 실패: key={}, error={}", key, e.getMessage());
        }
    }
    
    /**
     * 캐시된 즐겨찾기 목록 조회
     */
    public Optional<FavoriteListResponse> getCachedFavorites(String userId, FavoriteSearchRequest request) {
        String key = buildCacheKey(userId, request);
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.debug("즐겨찾기 캐시 히트: key={}", key);
                return Optional.of((FavoriteListResponse) cached);
            }
        } catch (Exception e) {
            log.warn("즐겨찾기 캐시 조회 실패: key={}, error={}", key, e.getMessage());
        }
        return Optional.empty();
    }
    
    /**
     * 사용자의 즐겨찾기 통계 캐싱
     */
    public void cacheUserFavoriteStats(String userId, Object stats) {
        String key = FAVORITE_STATS_CACHE_KEY + userId;
        try {
            redisTemplate.opsForValue().set(key, stats, CACHE_TTL);
            log.debug("즐겨찾기 통계 캐시 저장: key={}", key);
        } catch (Exception e) {
            log.warn("즐겨찾기 통계 캐시 저장 실패: key={}, error={}", key, e.getMessage());
        }
    }
    
    /**
     * 캐시된 즐겨찾기 통계 조회
     */
    public Optional<Object> getCachedFavoriteStats(String userId) {
        String key = FAVORITE_STATS_CACHE_KEY + userId;
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.debug("즐겨찾기 통계 캐시 히트: key={}", key);
                return Optional.of(cached);
            }
        } catch (Exception e) {
            log.warn("즐겨찾기 통계 캐시 조회 실패: key={}, error={}", key, e.getMessage());
        }
        return Optional.empty();
    }
    
    /**
     * 사용자의 즐겨찾기 캐시 무효화
     */
    public void invalidateUserFavorites(String userId) {
        try {
            // 사용자의 모든 즐겨찾기 관련 캐시 삭제
            String pattern = FAVORITE_CACHE_KEY + userId + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            // 통계 캐시도 삭제
            String statsKey = FAVORITE_STATS_CACHE_KEY + userId;
            redisTemplate.delete(statsKey);
            
            log.debug("즐겨찾기 캐시 무효화 완료: userId={}", userId);
        } catch (Exception e) {
            log.warn("즐겨찾기 캐시 무효화 실패: userId={}, error={}", userId, e.getMessage());
        }
    }
    
    /**
     * 특정 상품의 즐겨찾기 캐시 무효화
     */
    public void invalidateProductFavorites(Long productId) {
        try {
            // 해당 상품을 즐겨찾기한 모든 사용자의 캐시 무효화
            String pattern = FAVORITE_CACHE_KEY + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            log.debug("상품 즐겨찾기 캐시 무효화 완료: productId={}", productId);
        } catch (Exception e) {
            log.warn("상품 즐겨찾기 캐시 무효화 실패: productId={}, error={}", productId, e.getMessage());
        }
    }
    
    /**
     * 캐시 키 생성
     */
    private String buildCacheKey(String userId, FavoriteSearchRequest request) {
        StringBuilder keyBuilder = new StringBuilder(FAVORITE_CACHE_KEY + userId);
        
        if (request.category() != null) {
            keyBuilder.append(":cat:").append(request.category());
        }
        if (request.brand() != null) {
            keyBuilder.append(":brand:").append(request.brand());
        }
        if (request.mallName() != null) {
            keyBuilder.append(":mall:").append(request.mallName());
        }
        if (request.sortBy() != null) {
            keyBuilder.append(":sort:").append(request.sortBy());
        }
        keyBuilder.append(":page:").append(request.page()).append(":size:").append(request.size());
        
        return keyBuilder.toString();
    }
}
