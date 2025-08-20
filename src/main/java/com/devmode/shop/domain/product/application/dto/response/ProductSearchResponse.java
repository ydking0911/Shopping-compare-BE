package com.devmode.shop.domain.product.application.dto.response;

import java.util.List;

public record ProductSearchResponse(
    String keyword,
    Integer page,
    Integer size,
    Integer totalResults,
    Integer currentPage,
    Integer pageSize,
    Integer totalPages,
    String sort,
    String source,
    List<String> appliedFilters,
    List<ProductItem> products, // NaverProductItem을 ProductItem으로 변경
    SearchMetadata metadata
) {
    public ProductSearchResponse {
        // 기본값 설정
        page = (page != null && page > 0) ? page : 1;
        size = (size != null && size > 0) ? size : 20;
        currentPage = (currentPage != null) ? currentPage : page;
        pageSize = (pageSize != null) ? pageSize : size;
        source = (source != null) ? source : "fresh";
        
        // null 리스트를 빈 리스트로 변환
        appliedFilters = (appliedFilters != null) ? appliedFilters : List.of();
        products = (products != null) ? products : List.of();
        
        // totalPages 계산
        if (totalResults != null && size != null && size > 0) {
            totalPages = (int) Math.ceil((double) totalResults / size);
        } else {
            totalPages = 1;
        }
    }
    
    public record SearchMetadata(
        String lastBuildDate,
        String cacheStatus, // "fresh", "cached", "fallback"
        Long responseTime,
        Integer apiCallCount,
        String quotaStatus // "available", "warning", "exceeded"
    ) {
        public SearchMetadata {
            // 기본값 설정
            cacheStatus = (cacheStatus != null) ? cacheStatus : "fresh";
            responseTime = (responseTime != null) ? responseTime : 0L;
            apiCallCount = (apiCallCount != null) ? apiCallCount : 0;
            quotaStatus = (quotaStatus != null) ? quotaStatus : "available";
        }
    }
    
    // 테스트용 간단한 생성 메서드
    public static ProductSearchResponse of(String keyword, List<ProductItem> products) {
        return new ProductSearchResponse(
            keyword, 1, 20, 150, 1, 20, 8, "sim", "fresh",
            List.of(), products, null
        );
    }
}
