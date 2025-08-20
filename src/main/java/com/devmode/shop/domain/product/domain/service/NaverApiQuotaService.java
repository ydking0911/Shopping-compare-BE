package com.devmode.shop.domain.product.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverApiQuotaService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String QUOTA_PREFIX = "NAVER_API_QUOTA:";
    private static final String DAILY_COUNT_PREFIX = "NAVER_API_DAILY_COUNT:";
    private static final int MAX_DAILY_CALLS = 25000; // 네이버 쇼핑 API 일일 제한
    private static final int WARNING_THRESHOLD = 20000; // 경고 임계값
    
    public boolean canMakeApiCall() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String dailyCountKey = DAILY_COUNT_PREFIX + today;
        
        String currentCountStr = redisTemplate.opsForValue().get(dailyCountKey);
        int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;
        
        return currentCount < MAX_DAILY_CALLS;
    }
    
    public void incrementApiCallCount() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String dailyCountKey = DAILY_COUNT_PREFIX + today;
        
        Long newCount = redisTemplate.opsForValue().increment(dailyCountKey);
        
        // TTL 설정 (다음날 자정까지)
        if (newCount != null && newCount == 1) {
            redisTemplate.expire(dailyCountKey, java.time.Duration.ofDays(1));
        }
        
        log.info("[NaverApiQuota] API call count incremented. Today's count: {}", newCount);
        
        // 경고 임계값 체크
        if (newCount != null && newCount >= WARNING_THRESHOLD) {
            log.warn("[NaverApiQuota] WARNING: API call count approaching limit. Current: {}, Limit: {}", newCount, MAX_DAILY_CALLS);
        }
        
        // 제한 도달 시 경고
        if (newCount != null && newCount >= MAX_DAILY_CALLS) {
            log.error("[NaverApiQuota] CRITICAL: Daily API call limit reached!");
        }
    }
    
    public int getCurrentDailyCount() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String dailyCountKey = DAILY_COUNT_PREFIX + today;
        
        String currentCountStr = redisTemplate.opsForValue().get(dailyCountKey);
        return currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;
    }
    
    public int getRemainingCalls() {
        return Math.max(0, MAX_DAILY_CALLS - getCurrentDailyCount());
    }
    
    public String getQuotaStatus() {
        int currentCount = getCurrentDailyCount();
        
        if (currentCount >= MAX_DAILY_CALLS) {
            return "exceeded";
        } else if (currentCount >= WARNING_THRESHOLD) {
            return "warning";
        } else {
            return "available";
        }
    }
    
    public void resetDailyCount() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String dailyCountKey = DAILY_COUNT_PREFIX + today;
        
        redisTemplate.delete(dailyCountKey);
        log.info("[NaverApiQuota] Daily count reset for: {}", today);
    }
    
    public boolean isQuotaExceeded() {
        return !canMakeApiCall();
    }
}
