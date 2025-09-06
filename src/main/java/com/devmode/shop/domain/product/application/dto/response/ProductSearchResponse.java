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
        String quotaStatus, // "available", "warning", "exceeded"
        Boolean aiApplied, // AI가 적용되었는지 여부
        String aiOriginalKeyword, // AI 적용 전 원본 검색어
        String aiEnhancedKeyword, // AI가 개선한 검색어
        List<String> aiRelatedKeywords, // AI가 제안한 관련 키워드들
        String aiSearchTip, // AI가 제안한 검색 팁
        String aiCategorySuggestion // AI가 제안한 카테고리
    ) {
        public SearchMetadata {
            // 기본값 설정
            cacheStatus = (cacheStatus != null) ? cacheStatus : "fresh";
            responseTime = (responseTime != null) ? responseTime : 0L;
            apiCallCount = (apiCallCount != null) ? apiCallCount : 0;
            quotaStatus = (quotaStatus != null) ? quotaStatus : "available";
            aiApplied = (aiApplied != null) ? aiApplied : false;
            aiOriginalKeyword = (aiOriginalKeyword != null) ? aiOriginalKeyword : "";
            aiEnhancedKeyword = (aiEnhancedKeyword != null) ? aiEnhancedKeyword : "";
            aiRelatedKeywords = (aiRelatedKeywords != null) ? aiRelatedKeywords : List.of();
            aiSearchTip = (aiSearchTip != null) ? aiSearchTip : "";
            aiCategorySuggestion = (aiCategorySuggestion != null) ? aiCategorySuggestion : "";
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
