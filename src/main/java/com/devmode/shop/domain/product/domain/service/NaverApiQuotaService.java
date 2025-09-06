package com.devmode.shop.domain.product.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverApiQuotaService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String QUOTA_PREFIX = "NAVER_API_QUOTA:";
    private static final String DAILY_COUNT_PREFIX = "NAVER_API_DAILY_COUNT:";
    private static final String USER_DAILY_COUNT_PREFIX = "NAVER_API_USER_DAILY_COUNT:";
    private static final String IP_DAILY_COUNT_PREFIX = "NAVER_API_IP_DAILY_COUNT:";

    @Value("${naver.shopping.max-daily-calls:25000}")
    private int maxDailyCalls;

    @Value("${naver.shopping.warning-threshold:20000}")
    private int warningThreshold;

    // Per-user/IP daily limits for general search
    @Value("${rate-limit.user.anonymous-daily-limit:100}")
    private int anonymousDailyLimit;

    @Value("${rate-limit.user.authenticated-daily-limit:200}")
    private int authenticatedDailyLimit;
    
    public boolean canMakeApiCall() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String dailyCountKey = DAILY_COUNT_PREFIX + today;
        
        String currentCountStr = redisTemplate.opsForValue().get(dailyCountKey);
        int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;
        
        boolean canMakeCall = currentCount < maxDailyCalls;
        
        log.info("[NaverApiQuota] Checking quota - Date: {}, Current: {}, Limit: {}, Can make call: {}", 
                today, currentCount, maxDailyCalls, canMakeCall);
        
        if (!canMakeCall) {
            log.error("[NaverApiQuota] QUOTA EXCEEDED - Daily limit reached: {}/{}", currentCount, maxDailyCalls);
        }
        
        return canMakeCall;
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
        if (newCount != null && newCount >= warningThreshold) {
            log.warn("[NaverApiQuota] WARNING: API call count approaching limit. Current: {}, Limit: {}", newCount, maxDailyCalls);
        }
        
        // 제한 도달 시 경고
        if (newCount != null && newCount >= maxDailyCalls) {
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
        return Math.max(0, maxDailyCalls - getCurrentDailyCount());
    }
    
    public String getQuotaStatus() {
        int currentCount = getCurrentDailyCount();
        
        if (currentCount >= maxDailyCalls) {
            return "exceeded";
        } else if (currentCount >= warningThreshold) {
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

    // --- Per-user/IP daily limit for general search ---
    public void enforceAndIncrementPerUserOrIpLimit(String userIdOrNull, String ipAddress) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        boolean isAuthenticatedUser = userIdOrNull != null && !userIdOrNull.isBlank();
        String key;
        int limit;

        if (isAuthenticatedUser) {
            key = USER_DAILY_COUNT_PREFIX + today + ":" + userIdOrNull;
            limit = authenticatedDailyLimit;
        } else {
            key = IP_DAILY_COUNT_PREFIX + today + ":" + (ipAddress != null ? ipAddress : "unknown");
            limit = anonymousDailyLimit;
        }

        String currentStr = redisTemplate.opsForValue().get(key);
        int current = currentStr != null ? Integer.parseInt(currentStr) : 0;
        if (current >= limit) {
            throw new RuntimeException("일일 검색 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");
        }
        Long newCount = redisTemplate.opsForValue().increment(key);
        if (newCount != null && newCount == 1) {
            redisTemplate.expire(key, java.time.Duration.ofDays(1));
        }
        log.info("[NaverApiQuota] Per-{} limit incremented: {}/{} (key={})",
                isAuthenticatedUser ? "user" : "ip", newCount, limit, key);
    }
}
