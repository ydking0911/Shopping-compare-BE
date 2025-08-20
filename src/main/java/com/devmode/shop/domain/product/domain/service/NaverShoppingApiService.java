package com.devmode.shop.domain.product.domain.service;

import com.devmode.shop.domain.product.application.dto.request.ProductSearchRequest;
import com.devmode.shop.domain.product.application.dto.response.NaverShoppingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
            
            // RestTemplate으로는 헤더를 쉽게 설정할 수 없으므로, 
            // HttpHeaders와 HttpEntity를 사용하거나 다른 방법을 고려해야 합니다.
            // 현재는 기본 호출만 구현합니다.
            NaverShoppingResponse response = restTemplate.getForObject(url, NaverShoppingResponse.class);
            
            if (response != null) {
                log.info("[NaverShoppingApi] Search successful. Total results: {}", response.total());
            }
            
            return response;
        } catch (RestClientException e) {
            log.error("[NaverShoppingApi] Search failed for keyword: {}. Error: {}", request.keyword(), e.getMessage());
            throw e;
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
            urlBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        
        return urlBuilder.toString();
    }
    
    private Map<String, String> buildQueryParams(ProductSearchRequest request, String encodedKeyword) {
        Map<String, String> params = new HashMap<>();
        
        params.put("query", encodedKeyword);
        params.put("display", String.valueOf(request.size()));
        params.put("start", String.valueOf((request.page() - 1) * request.size() + 1));
        params.put("sort", request.sort());
        
        // 필터 적용
        if (request.excludeFilters() != null) {
            for (String filter : request.excludeFilters()) {
                switch (filter.toLowerCase()) {
                    case "used":
                        params.put("exclude", "used");
                        break;
                    case "rental":
                        params.put("exclude", "rental");
                        break;
                    case "overseas":
                        params.put("exclude", "overseas");
                        break;
                }
            }
        }
        
        if (request.onlyNPay() != null && request.onlyNPay()) {
            params.put("npay", "1");
        }
        
        if (request.category1() != null) {
            params.put("category1", request.category1());
        }
        
        if (request.category2() != null) {
            params.put("category2", request.category2());
        }
        
        if (request.category3() != null) {
            params.put("category3", request.category3());
        }
        
        if (request.category4() != null) {
            params.put("category4", request.category4());
        }
        
        if (request.brand() != null) {
            params.put("brand", request.brand());
        }
        
        if (request.mallName() != null) {
            params.put("mallName", request.mallName());
        }
        
        if (request.minPrice() != null) {
            params.put("minPrice", String.valueOf(request.minPrice()));
        }
        
        if (request.maxPrice() != null) {
            params.put("maxPrice", String.valueOf(request.maxPrice()));
        }
        
        return params;
    }
}
