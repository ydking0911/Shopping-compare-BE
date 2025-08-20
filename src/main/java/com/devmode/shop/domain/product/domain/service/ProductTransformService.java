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
            long responseTime,
            int apiCallCount,
            String quotaStatus) {
        
        // NaverProductItem을 정규화된 ProductItem으로 변환
        List<ProductItem> productItems = naverResponse.items().stream()
                .map(naverItem -> {
                    int searchRank = naverResponse.items().indexOf(naverItem) + 1;
                    return normalizationService.normalizeProductItem(naverItem, keyword, appliedFilters, searchRank);
                })
                .collect(Collectors.toList());
        
        int totalResults = naverResponse.total() != null ? naverResponse.total() : 0;
        int totalPages = (int) Math.ceil((double) totalResults / size);
        
        ProductSearchResponse.SearchMetadata metadata = new ProductSearchResponse.SearchMetadata(
            naverResponse.lastBuildDate(),
            cacheStatus,
            responseTime,
            apiCallCount,
            quotaStatus
        );

        return new ProductSearchResponse(
            keyword, page, size, totalResults, page, size, totalPages, sort, cacheStatus,
            appliedFilters, productItems, metadata
        );
    }
}
