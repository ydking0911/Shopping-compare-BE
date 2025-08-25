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

import java.util.List;
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

    /**
     * DataLab 상위 키워드 로딩 (시드용)
     * 실제 구현에서는 DataLab 인기 키워드 API를 호출하도록 확장 필요
     */
    public List<String> fetchTopKeywords() {
        return List.of("아이폰", "갤럭시", "맥북", "에어팟", "노트북", "청소기", "모니터", "그래픽카드", "TV", "냉장고");
    }

    /**
     * DataLab 상위 키워드 로딩 (시드용) - 개수 지정
     */
    public List<String> fetchTopKeywords(int count) {
        List<String> allKeywords = List.of("아이폰", "갤럭시", "맥북", "에어팟", "노트북", "청소기", "모니터", "그래픽카드", "TV", "냉장고");
        return allKeywords.stream().limit(count).toList();
    }

    /**
     * 특정 키워드의 트렌드 데이터 가져오기
     */
    public Object fetchTrendData(String keyword) {
        try {
            // 실제 구현에서는 DataLab API를 호출하여 키워드별 트렌드 데이터를 가져옴
            // 현재는 목업 데이터 반환
            return Map.of(
                "keyword", keyword,
                "timestamp", System.currentTimeMillis(),
                "trendScore", Math.random() * 100,
                "searchVolume", (int)(Math.random() * 10000)
            );
        } catch (Exception e) {
            // 로깅만 하고 null 반환 (프리페치 실패 시 전체 프로세스 중단 방지)
            return null;
        }
    }
}
