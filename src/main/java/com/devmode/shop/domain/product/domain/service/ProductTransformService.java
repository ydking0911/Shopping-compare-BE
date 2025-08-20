package com.devmode.shop.domain.product.domain.service;

import com.devmode.shop.domain.product.application.dto.response.NaverShoppingResponse;
import com.devmode.shop.domain.product.application.dto.response.NaverProductItem;
import com.devmode.shop.domain.product.application.dto.response.ProductSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductTransformService {
    
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
        
        // NaverProductItem을 직접 사용하고, 필요한 데이터 변환만 적용
        List<NaverProductItem> productItems = naverResponse.items().stream()
                .map(this::enrichNaverProductItem)
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
            keyword, page, size, totalResults, page, size, null, sort, cacheStatus,
            appliedFilters, productItems, metadata
        );
    }
    
    /**
     * NaverProductItem에 추가 정보를 보강하는 메서드
     * 기존 데이터는 그대로 유지하고, 필요한 경우에만 변환
     */
    private NaverProductItem enrichNaverProductItem(NaverProductItem naverItem) {
        // 현재는 NaverProductItem을 그대로 반환
        // 필요시 추가 로직을 여기에 구현
        return naverItem;
    }
}
