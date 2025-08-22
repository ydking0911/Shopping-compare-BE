package com.devmode.shop.domain.trend.domain.service;

import com.devmode.shop.domain.trend.application.dto.response.datalab.NaverDataLabResponse;
import com.devmode.shop.domain.trend.application.dto.request.TrendSearchRequest;
import com.devmode.shop.global.config.properties.DataLabApiProperties;
import com.devmode.shop.global.exception.RestApiException;
import com.devmode.shop.global.exception.code.status.GlobalErrorStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NaverDataLabApiService {

    private final RestTemplate restTemplate;
    private final DataLabApiProperties dataLabApiProperties;
    private final ObjectMapper objectMapper;

    public NaverDataLabResponse searchTrends(TrendSearchRequest request) {
        try {
            // 1. API 요청 URL 및 헤더 설정
            String url = buildApiUrl(request);
            HttpHeaders headers = buildHeaders();
            
            // 2. API 호출
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, 
                    HttpMethod.GET, 
                    entity, 
                    String.class
            );
            
            // 3. 응답 파싱
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), NaverDataLabResponse.class);
            } else {
                throw new RestApiException(GlobalErrorStatus._INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e) {
            throw new RestApiException(GlobalErrorStatus._INTERNAL_SERVER_ERROR);
        }
    }

    private String buildApiUrl(TrendSearchRequest request) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(dataLabApiProperties.getApiUrl())
                .queryParam("startDate", request.startDate())
                .queryParam("endDate", request.endDate())
                .queryParam("timeUnit", request.timeUnit() != null ? request.timeUnit() : "date");

        // 카테고리 추가
        if (request.categories() != null && !request.categories().isEmpty()) {
            builder.queryParam("category", String.join(",", request.categories()));
        }

        // 키워드 추가
        if (request.keywords() != null && !request.keywords().isEmpty()) {
            builder.queryParam("keyword", String.join(",", request.keywords()));
        }

        // 디바이스 분포 포함 여부
        if (request.includeDeviceDistribution()) {
            builder.queryParam("device", "pc,mo");
        }

        // 성별 분포 포함 여부
        if (request.includeGenderDistribution()) {
            builder.queryParam("gender", "f,m");
        }

        // 연령 분포 포함 여부
        if (request.includeAgeDistribution()) {
            builder.queryParam("ages", "10,20,30,40,50,60");
        }

        return builder.toUriString();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", dataLabApiProperties.getClientId());
        headers.set("X-Naver-Client-Secret", dataLabApiProperties.getClientSecret());
        headers.set("Content-Type", "application/json");
        return headers;
    }
}
