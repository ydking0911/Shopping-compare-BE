package com.devmode.shop.domain.product.domain.service;

import com.devmode.shop.domain.product.application.dto.response.NaverShoppingResponse;
import com.devmode.shop.domain.product.application.dto.response.NaverProductItem;
import com.devmode.shop.domain.product.application.dto.response.ProductItem;
import com.devmode.shop.domain.product.application.dto.response.ProductSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductTransformService {
    
    private final ProductNormalizationService normalizationService;
    
    public ProductSearchResponse transformToProductSearchResponse(
            NaverShoppingResponse naverResponse, 
            String keyword, 
            int page, 
            int size, 
            String sort,
            List<String> appliedFilters,
            String cacheStatus,
            Long responseTime,
            Integer apiCallCount,
            String quotaStatus) {
        
        // NaverProductItem을 정규화된 ProductItem으로 변환
        List<ProductItem> productItems = naverResponse.items().stream()
                .map(naverItem -> {
                    int searchRank = naverResponse.items().indexOf(naverItem) + 1;
                    return normalizationService.normalizeProductItem(naverItem, keyword, appliedFilters, searchRank);
                })
                .collect(Collectors.toList());
        
        int totalResults = naverResponse.total() != null ? naverResponse.total() : 0;
        
        // 너무 많은 검색 결과는 제한 (API 할당량 보호 및 사용자 경험 개선)
        int maxReasonableResults = 10000; // 최대 1만개로 제한
        if (totalResults > maxReasonableResults) {
            log.warn("[ProductTransform] 검색 결과가 너무 많음: keyword={}, totalResults={}, 제한됨={}", 
                keyword, totalResults, maxReasonableResults);
            totalResults = maxReasonableResults;
        }
        
        int totalPages = (int) Math.ceil((double) totalResults / size);
        
        ProductSearchResponse.SearchMetadata metadata = new ProductSearchResponse.SearchMetadata(
            naverResponse.lastBuildDate(),
            cacheStatus,
            responseTime,
            apiCallCount,
            quotaStatus,
            false, // aiApplied - 기본 검색이므로 false
            "", // aiOriginalKeyword - 기본 검색이므로 빈 문자열
            "", // aiEnhancedKeyword - 기본 검색이므로 빈 문자열
            List.of(), // aiRelatedKeywords - 기본 검색이므로 빈 리스트
            "", // aiSearchTip - 기본 검색이므로 빈 문자열
            "" // aiCategorySuggestion - 기본 검색이므로 빈 문자열
        );

        return new ProductSearchResponse(
            keyword, page, size, totalResults, page, size, totalPages, sort, cacheStatus,
            appliedFilters, productItems, metadata
        );
    }
}
