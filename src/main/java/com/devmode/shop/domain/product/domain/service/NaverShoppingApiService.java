package com.devmode.shop.domain.product.domain.service;

import com.devmode.shop.domain.product.application.dto.request.ProductSearchRequest;
import com.devmode.shop.domain.product.application.dto.response.NaverShoppingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverShoppingApiService {
    
    private final RestTemplate restTemplate;
    
    @Value("${naver.shopping.client-id}")
    private String clientId;
    
    @Value("${naver.shopping.client-secret}")
    private String clientSecret;
    
    @Value("${naver.shopping.api-url:https://openapi.naver.com/v1/search/shop.json}")
    private String apiUrl;
    
    public NaverShoppingResponse searchProducts(ProductSearchRequest request) {
        String encodedKeyword = URLEncoder.encode(request.keyword(), StandardCharsets.UTF_8);
        
        Map<String, String> queryParams = buildQueryParams(request, encodedKeyword);
        
        log.info("[NaverShoppingApi] Searching products with keyword: {}, params: {}", request.keyword(), queryParams);
        
        try {
            String url = buildUrl(apiUrl, queryParams);
            log.info("[NaverShoppingApi] Built URL: {}", url);
            
            // 네이버 API 호출 시 필수/권장 헤더 (curl과 최대한 동일하게)
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", clientId);
            headers.set("X-Naver-Client-Secret", clientSecret);
            // 브라우저 유사 헤더 추가 (일부 케이스에서 total=0 회피)
            headers.set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36");
            headers.set("Accept", "application/json, text/plain, */*");
            headers.set("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
            headers.set("Accept-Encoding", "gzip");
            headers.set("Referer", "https://search.shopping.naver.com/");
            headers.set("Origin", "https://search.shopping.naver.com");
            headers.set("X-Requested-With", "XMLHttpRequest");
            // 요청 로그(마스킹) 출력
            log.info("[NaverShoppingApi] Final URL: {}", url);
            log.info("[NaverShoppingApi] Request Headers: {}", maskSensitiveHeaders(headers));

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            log.info("[NaverShoppingApi] Making API call with headers: X-Naver-Client-Id={}", clientId);
            log.info("[NaverShoppingApi] Client Secret length: {}", clientSecret != null ? clientSecret.length() : "null");
            log.info("[NaverShoppingApi] Client ID length: {}", clientId != null ? clientId.length() : "null");
            
            log.info("[NaverShoppingApi] Sending HTTP request to: {}", url);
            
            // 1차: JDK HttpClient(HTTP/2)로 호출하여 curl과 최대한 동일 경로 사용
            NaverShoppingResponse response = executeWithJdkHttpClient(url);
            if (response == null || response.items() == null || response.items().isEmpty()) {
                log.warn("[NaverShoppingApi] JDK HttpClient returned empty items. Falling back to RestTemplate.");
                // Fallback: RestTemplate로 수신 후 파싱
                ResponseEntity<String> rawResponseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
                );
                log.info("[NaverShoppingApi] [Fallback] HTTP Response Status: {}", rawResponseEntity.getStatusCode());
                log.info("[NaverShoppingApi] [Fallback] HTTP Response Headers: {}", rawResponseEntity.getHeaders());
                log.info("[NaverShoppingApi] [Fallback] Raw response body: {}", rawResponseEntity.getBody());

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    response = mapper.readValue(rawResponseEntity.getBody(), NaverShoppingResponse.class);
                } catch (Exception parseEx) {
                    log.error("[NaverShoppingApi] Failed to parse raw response: {}", parseEx.getMessage(), parseEx);
                    throw parseEx;
                }
            }
            
            if (response != null) {
                log.info("[NaverShoppingApi] Response parsing successful");
                log.info("[NaverShoppingApi] Total results: {}", response.total());
                log.info("[NaverShoppingApi] Items count: {}", response.items() != null ? response.items().size() : 0);
                log.info("[NaverShoppingApi] Response object: {}", response);
                
                if (response.items() != null && !response.items().isEmpty()) {
                    log.info("[NaverShoppingApi] First item title: {}", response.items().get(0).title());
                    log.info("[NaverShoppingApi] First item price: {}", response.items().get(0).lprice());
                } else {
                    log.warn("[NaverShoppingApi] Items list is null or empty");
                }
            } else {
                log.error("[NaverShoppingApi] Response body is null - parsing failed");
            }
            
            return response;
        } catch (RestClientException e) {
            log.error("[NaverShoppingApi] RestClientException for keyword: {}", request.keyword());
            log.error("[NaverShoppingApi] Error message: {}", e.getMessage());
            log.error("[NaverShoppingApi] Error class: {}", e.getClass().getSimpleName());
            log.error("[NaverShoppingApi] Root cause: {}", e.getRootCause() != null ? e.getRootCause().getMessage() : "No root cause");
            log.error("[NaverShoppingApi] Full stack trace:", e);
            throw e;
        } catch (Exception e) {
            log.error("[NaverShoppingApi] Unexpected error during search for keyword: {}", request.keyword());
            log.error("[NaverShoppingApi] Error message: {}", e.getMessage());
            log.error("[NaverShoppingApi] Error class: {}", e.getClass().getSimpleName());
            log.error("[NaverShoppingApi] Full stack trace:", e);
            throw new RuntimeException("Failed to search products", e);
        }
    }
    
    private String maskSensitiveHeaders(HttpHeaders headers) {
        try {
            java.util.Map<String, java.util.List<String>> map = new java.util.LinkedHashMap<>();
            for (java.util.Map.Entry<String, java.util.List<String>> e : headers.entrySet()) {
                String key = e.getKey();
                java.util.List<String> vals = e.getValue();
                if ("X-Naver-Client-Id".equalsIgnoreCase(key) || "X-Naver-Client-Secret".equalsIgnoreCase(key)) {
                    java.util.List<String> masked = new java.util.ArrayList<>();
                    for (String v : vals) {
                        masked.add(maskToken(v));
                    }
                    map.put(key, masked);
                } else {
                    map.put(key, vals);
                }
            }
            return map.toString();
        } catch (Exception ex) {
            return "<header-mask-error>";
        }
    }

    private String maskToken(String token) {
        if (token == null) return null;
        int len = token.length();
        if (len <= 4) return "****";
        String prefix = token.substring(0, 4);
        return prefix + "****";
    }
    private NaverShoppingResponse executeWithJdkHttpClient(String url) {
        try {
            HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Accept-Encoding", "gzip")
                .header("Referer", "https://search.shopping.naver.com/")
                .header("Origin", "https://search.shopping.naver.com")
                .header("X-Requested-With", "XMLHttpRequest")
                .GET()
                .build();
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            log.info("[NaverShoppingApi][JDK] HTTP Status: {}", httpResponse.statusCode());
            log.info("[NaverShoppingApi][JDK] Raw body: {}", httpResponse.body());
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(httpResponse.body(), NaverShoppingResponse.class);
        } catch (Exception e) {
            log.error("[NaverShoppingApi][JDK] Request failed: {}", e.getMessage(), e);
            return null;
        }
    }
    
    private String buildUrl(String baseUrl, Map<String, String> params) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?");

        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                urlBuilder.append("&");
            }
            String key = entry.getKey();
            String value = entry.getValue();
            // query만 선인코딩되어 있으므로 그대로 사용, 나머지는 UTF-8로 명시 인코딩
            if (!"query".equals(key)) {
                value = URLEncoder.encode(value, StandardCharsets.UTF_8);
            }
            urlBuilder.append(key).append("=").append(value);
            first = false;
        }

        return urlBuilder.toString();
    }
    
    private Map<String, String> buildQueryParams(ProductSearchRequest request, String encodedKeyword) {
        Map<String, String> params = new HashMap<>();
        
        // 네이버 공식 스펙: query, display, start, sort, filter, exclude 만 사용
        params.put("query", encodedKeyword);
        params.put("display", String.valueOf(request.size()));
        params.put("start", String.valueOf((request.page() - 1) * request.size() + 1));
        params.put("sort", request.sort());

        // filter: naverpay (NPay만)
        if (request.onlyNPay() != null && request.onlyNPay()) {
            params.put("filter", "naverpay");
        }

        // exclude: used / rental / cbshop(해외직구)
        if (request.excludeFilters() != null && !request.excludeFilters().isEmpty()) {
            java.util.List<String> mappedExcludes = new java.util.ArrayList<>();
            for (String raw : request.excludeFilters()) {
                if (raw == null) continue;
                String filter = raw.trim().toLowerCase();
                switch (filter) {
                    case "used":
                        mappedExcludes.add("used");
                        break;
                    case "rental":
                        mappedExcludes.add("rental");
                        break;
                    case "overseas":
                    case "cbshop":
                        mappedExcludes.add("cbshop");
                        break;
                    default:
                        // 스펙 외 값은 무시
                        break;
                }
            }
            if (!mappedExcludes.isEmpty()) {
                // 콜론으로 합침 (e.g., used:rental:cbshop)
                params.put("exclude", String.join(":", mappedExcludes));
            }
        }

        return params;
    }
    
    /**
     * 상품 ID로 상품 정보 조회
     */
    public NaverShoppingResponse searchProductById(String productId) {
        log.info("[NaverShoppingApi] Searching product by ID: {}", productId);
        
        try {
            // 상품 ID로 검색하는 경우, 정확한 매칭을 위해 상품명으로 검색
            // 실제로는 네이버 API에서 상품 ID로 직접 조회하는 방법이 없어서
            // 상품명이나 다른 식별자로 검색하는 방식 사용
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("query", productId);
            queryParams.put("display", "1");
            queryParams.put("start", "1");
            queryParams.put("sort", "sim"); // 정확도순 정렬
            
            String url = buildUrl(apiUrl, queryParams);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", clientId);
            headers.set("X-Naver-Client-Secret", clientSecret);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<NaverShoppingResponse> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                NaverShoppingResponse.class
            );
            
            NaverShoppingResponse response = responseEntity.getBody();
            if (response != null) {
                log.info("[NaverShoppingApi] Product search by ID successful. Found: {}", response.total());
            }
            
            return response;
        } catch (RestClientException e) {
            log.error("[NaverShoppingApi] Product search by ID failed for productId: {}. Error: {}", productId, e.getMessage());
            throw e;
        }
    }
}
