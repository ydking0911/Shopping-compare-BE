package com.devmode.shop.domain.product.application.usecase;

import com.devmode.shop.domain.product.application.dto.request.ProductSearchRequest;
import com.devmode.shop.domain.product.application.dto.response.NaverShoppingResponse;
import com.devmode.shop.domain.product.application.dto.response.ProductSearchResponse;
import com.devmode.shop.domain.product.domain.service.NaverApiQuotaService;
import com.devmode.shop.domain.product.domain.service.NaverShoppingApiService;
import com.devmode.shop.domain.product.domain.service.ProductCacheService;
import com.devmode.shop.domain.product.domain.service.ProductTransformService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductSearchUseCase {
    
    private static final Logger log = LoggerFactory.getLogger(ProductSearchUseCase.class);
    
    private final NaverShoppingApiService naverShoppingApiService;
    private final ProductCacheService productCacheService;
    private final NaverApiQuotaService quotaService;
    private final ProductTransformService transformService;
    
    public ProductSearchResponse searchProducts(ProductSearchRequest request) {
        long startTime = System.currentTimeMillis();
        // 0. 유저/IP 기반 레이트리밋 (일반 검색)
        try {
            String userId = getAuthenticatedUserIdOrNull();
            String ip = getClientIp();
            quotaService.enforceAndIncrementPerUserOrIpLimit(userId, ip);
        } catch (RuntimeException limitEx) {
            log.warn("[ProductSearch] rate limit exceeded: {}", limitEx.getMessage());
            throw limitEx;
        }
        
        // 1. 캐시 확인
        if (productCacheService.isCached(request)) {
            return productCacheService.getCachedResult(request)
                    .orElseThrow(() -> new RuntimeException("Failed to retrieve cached result"));
        }
        
        // 2. 쿼터 확인
        log.info("[ProductSearch] Checking API quota before search");
        if (quotaService.isQuotaExceeded()) {
            log.error("[ProductSearch] API quota exceeded - cannot make search request");
            throw new RuntimeException("API quota exceeded for today");
        }
        log.info("[ProductSearch] API quota check passed - proceeding with search");
        
        try {
            // 3. 네이버 API 호출
            log.info("[ProductSearch] Calling Naver API with request: keyword={}, page={}, size={}", 
                    request.keyword(), request.page(), request.size());
            NaverShoppingResponse naverResponse = naverShoppingApiService.searchProducts(request);
            log.info("[ProductSearch] Naver API response received - total: {}", 
                    naverResponse != null ? naverResponse.total() : "null");
            
            // 4. 쿼터 증가
            quotaService.incrementApiCallCount();
            log.info("[ProductSearch] API call count incremented");
            
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
            
            // 6. 캐시 저장 (totalResults > 0 인 경우에만)
            if (response.totalResults() != null && response.totalResults() > 0) {
                productCacheService.cacheSearchResult(request, response);
            } else {
                log.info("[ProductCache] Skip caching because totalResults is 0.");
            }
            
            return response;
            
        } catch (Exception e) {
            // 7. 캐시 폴백 시도
            try {
                return productCacheService.getCachedResult(request)
                        .orElseThrow(() -> new RuntimeException("No cached result available for fallback"));
            } catch (Exception fallbackException) {
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
    
    private String getAuthenticatedUserIdOrNull() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
                return auth.getName();
            }
        } catch (Exception ignored) {}
        return null;
    }
    
    private String getClientIp() {
        try {
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes) {
                ServletRequestAttributes servletAttrs = (ServletRequestAttributes) attrs;
                HttpServletRequest req = servletAttrs.getRequest();
                String xff = req.getHeader("X-Forwarded-For");
                if (xff != null && !xff.isBlank() && !"unknown".equalsIgnoreCase(xff)) {
                    return xff.split(",")[0].trim();
                }
                String xri = req.getHeader("X-Real-IP");
                if (xri != null && !xri.isBlank() && !"unknown".equalsIgnoreCase(xri)) {
                    return xri;
                }
                return req.getRemoteAddr();
            }
        } catch (Exception ignored) {}
        return "unknown";
    }
}
