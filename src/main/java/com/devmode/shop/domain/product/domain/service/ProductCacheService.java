package com.devmode.shop.domain.product.domain.service;

import com.devmode.shop.domain.product.application.dto.request.ProductSearchRequest;
import com.devmode.shop.domain.product.application.dto.response.ProductSearchResponse;
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
public class ProductCacheService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String CACHE_PREFIX = "PRODUCT_SEARCH:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30); // 30분 캐시
    
    public void cacheSearchResult(ProductSearchRequest request, ProductSearchResponse response) {
        try {
            String cacheKey = generateCacheKey(request);
            String jsonResponse = objectMapper.writeValueAsString(response);
            
            redisTemplate.opsForValue().set(cacheKey, jsonResponse, CACHE_TTL);
            
            log.info("[ProductCache] Cached search result for key: {}", cacheKey);
        } catch (JsonProcessingException e) {
            log.error("[ProductCache] Failed to serialize search result: {}", e.getMessage());
        }
    }
    
    public Optional<ProductSearchResponse> getCachedResult(ProductSearchRequest request) {
        try {
            String cacheKey = generateCacheKey(request);
            String cachedJson = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedJson != null) {
                ProductSearchResponse response = objectMapper.readValue(cachedJson, ProductSearchResponse.class);
                log.info("[ProductCache] Cache hit for key: {}", cacheKey);
                return Optional.of(response);
            }
            
            log.info("[ProductCache] Cache miss for key: {}", cacheKey);
            return Optional.empty();
        } catch (JsonProcessingException e) {
            log.error("[ProductCache] Failed to deserialize cached result: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    public boolean isCached(ProductSearchRequest request) {
        String cacheKey = generateCacheKey(request);
        return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
    }
    
    public void invalidateCache(String keyword) {
        String pattern = CACHE_PREFIX + keyword + "*";
        // Redis에서 패턴 매칭으로 키를 찾아 삭제하는 로직
        // 실제 구현에서는 더 정교한 방법을 사용할 수 있습니다.
        log.info("[ProductCache] Invalidated cache for keyword: {}", keyword);
    }
    
    private String generateCacheKey(ProductSearchRequest request) {
        StringBuilder keyBuilder = new StringBuilder(CACHE_PREFIX);
        keyBuilder.append(request.keyword().toLowerCase());
        keyBuilder.append(":page=").append(request.page());
        keyBuilder.append(":size=").append(request.size());
        keyBuilder.append(":sort=").append(request.sort());
        
        if (request.excludeFilters() != null && !request.excludeFilters().isEmpty()) {
            keyBuilder.append(":exclude=").append(String.join(",", request.excludeFilters()));
        }
        
        if (request.onlyNPay() != null && request.onlyNPay()) {
            keyBuilder.append(":npay=1");
        }
        
        if (request.category1() != null) {
            keyBuilder.append(":cat1=").append(request.category1());
        }
        
        if (request.category2() != null) {
            keyBuilder.append(":cat2=").append(request.category2());
        }
        
        if (request.category3() != null) {
            keyBuilder.append(":cat3=").append(request.category3());
        }
        
        if (request.category4() != null) {
            keyBuilder.append(":cat4=").append(request.category4());
        }
        
        if (request.brand() != null) {
            keyBuilder.append(":brand=").append(request.brand());
        }
        
        if (request.mallName() != null) {
            keyBuilder.append(":mall=").append(request.mallName());
        }
        
        if (request.minPrice() != null) {
            keyBuilder.append(":minPrice=").append(request.minPrice());
        }
        
        if (request.maxPrice() != null) {
            keyBuilder.append(":maxPrice=").append(request.maxPrice());
        }
        
        if (request.minRating() != null) {
            keyBuilder.append(":minRating=").append(request.minRating());
        }
        
        if (request.minReviewCount() != null) {
            keyBuilder.append(":minReviewCount=").append(request.minReviewCount());
        }
        
        return keyBuilder.toString();
    }
}
