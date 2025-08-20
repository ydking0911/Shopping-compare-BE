package com.devmode.shop.domain.user.domain.service;

import java.time.Duration;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);
    private final RedisTemplate<String, String> redisTemplate;
    private final static String blacklistPrefix = "BLACKLIST:";

    public boolean isBlacklistToken(String token) {

        String savedToken = redisTemplate.opsForValue().get(blacklistPrefix + token);
        boolean blacklisted = savedToken != null && Objects.equals(savedToken, token);

        return blacklisted;
    }

    public void blacklist(String token, Duration expiration) {
        redisTemplate.opsForValue().set(blacklistPrefix + token, token, expiration);
    }
}