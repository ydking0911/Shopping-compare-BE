package com.devmode.shop.domain.product.application.usecase;

import com.devmode.shop.domain.product.application.dto.request.ProductSearchRequest;
import com.devmode.shop.domain.product.application.dto.response.NaverShoppingResponse;
import com.devmode.shop.domain.product.application.dto.response.ProductSearchResponse;
import com.devmode.shop.domain.product.domain.service.NaverApiQuotaService;
import com.devmode.shop.domain.product.domain.service.NaverShoppingApiService;
import com.devmode.shop.domain.product.domain.service.ProductCacheService;
import com.devmode.shop.domain.product.domain.service.ProductTransformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchUseCase {
    
    private final NaverShoppingApiService naverShoppingApiService;
    private final ProductCacheService productCacheService;
    private final NaverApiQuotaService quotaService;
    private final ProductTransformService transformService;
    
    public ProductSearchResponse searchProducts(ProductSearchRequest request) {
        long startTime = System.currentTimeMillis();
        
        log.info("[ProductSearchUseCase] Starting product search for keyword: {}", request.keyword());
        
        // 1. 캐시 확인
        if (productCacheService.isCached(request)) {
            log.info("[ProductSearchUseCase] Cache hit, returning cached result");
            return productCacheService.getCachedResult(request)
                    .orElseThrow(() -> new RuntimeException("Failed to retrieve cached result"));
        }
        
        // 2. 쿼터 확인
        if (quotaService.isQuotaExceeded()) {
            log.error("[ProductSearchUseCase] API quota exceeded, cannot make API call");
            throw new RuntimeException("API quota exceeded for today");
        }
        
        try {
            // 3. 네이버 API 호출
            log.info("[ProductSearchUseCase] Making API call to Naver Shopping API");
            NaverShoppingResponse naverResponse = naverShoppingApiService.searchProducts(request);
            
            // 4. 쿼터 증가
            quotaService.incrementApiCallCount();
            
            // 5. 응답 변환
            List<String> appliedFilters = buildAppliedFilters(request);
            ProductSearchResponse response = transformService.transformToProductSearchResponse(
                    naverResponse,
                    request.keyword(),
                    request.page(),
                    request.size(),
                    request.sort(),
                    appliedFilters,
                    "fresh",
                    System.currentTimeMillis() - startTime,
                    quotaService.getCurrentDailyCount(),
                    quotaService.getQuotaStatus()
            );
            
            // 6. 캐시 저장
            productCacheService.cacheSearchResult(request, response);
            
            log.info("[ProductSearchUseCase] Search completed successfully. Total results: {}", response.totalResults());
            
            return response;
            
        } catch (Exception e) {
            log.error("[ProductSearchUseCase] API call failed: {}", e.getMessage());
            
            // 7. 캐시 폴백 시도
            try {
                log.info("[ProductSearchUseCase] Attempting cache fallback");
                return productCacheService.getCachedResult(request)
                        .orElseThrow(() -> new RuntimeException("No cached result available for fallback"));
            } catch (Exception fallbackException) {
                log.error("[ProductSearchUseCase] Cache fallback also failed: {}", fallbackException.getMessage());
                throw new RuntimeException("Product search failed and no fallback available", e);
            }
        }
    }
    
    private List<String> buildAppliedFilters(ProductSearchRequest request) {
        List<String> filters = new ArrayList<>();
        
        if (request.excludeFilters() != null && !request.excludeFilters().isEmpty()) {
            filters.addAll(request.excludeFilters());
        }
        
        if (request.onlyNPay() != null && request.onlyNPay()) {
            filters.add("npay");
        }
        
        if (request.category1() != null) {
            filters.add("category1:" + request.category1());
        }
        
        if (request.category2() != null) {
            filters.add("category2:" + request.category2());
        }
        
        if (request.category3() != null) {
            filters.add("category3:" + request.category3());
        }
        
        if (request.category4() != null) {
            filters.add("category4:" + request.category4());
        }
        
        if (request.brand() != null) {
            filters.add("brand:" + request.brand());
        }
        
        if (request.mallName() != null) {
            filters.add("mall:" + request.mallName());
        }
        
        if (request.minPrice() != null && request.minPrice() > 0) {
            filters.add("minPrice:" + request.minPrice());
        }
        
        if (request.maxPrice() != null && request.maxPrice() > 0) {
            filters.add("maxPrice:" + request.maxPrice());
        }
        
        if (request.minRating() != null && request.minRating() > 0) {
            filters.add("minRating:" + request.minRating());
        }
        
        if (request.minReviewCount() != null && request.minReviewCount() > 0) {
            filters.add("minReviewCount:" + request.minReviewCount());
        }
        
        return filters;
    }
}
