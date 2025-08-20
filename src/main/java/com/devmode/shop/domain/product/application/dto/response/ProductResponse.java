package com.devmode.shop.domain.product.application.dto.response;

import com.devmode.shop.domain.product.application.dto.response.ProductSearchResponse;

/**
 * 상품 검색 결과를 간단하게 표현하는 응답 DTO
 */
public record ProductResponse(
    String keyword,
    Integer totalResults,
    Integer currentPage,
    Integer pageSize,
    String sort,
    String source
) {
    /**
     * ProductSearchResponse로부터 ProductResponse 생성
     */
    public static ProductResponse create(ProductSearchResponse response) {
        return new ProductResponse(
            response.keyword(),
            response.totalResults(),
            response.currentPage(),
            response.pageSize(),
            response.sort(),
            response.source()
        );
    }
    
    /**
     * 간단한 검색 결과 생성
     */
    public static ProductResponse of(String keyword, Integer totalResults) {
        return new ProductResponse(
            keyword, 
            totalResults, 
            1, 
            20, 
            "sim", 
            "fresh"
        );
    }
}
