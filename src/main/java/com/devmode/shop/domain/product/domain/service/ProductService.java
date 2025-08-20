package com.devmode.shop.domain.product.domain.service;

import com.devmode.shop.domain.product.application.dto.request.ProductSearchRequest;
import com.devmode.shop.domain.product.application.dto.response.ProductSearchResponse;
import com.devmode.shop.domain.product.application.usecase.ProductSearchUseCase;
import com.devmode.shop.global.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.devmode.shop.global.exception.code.status.GlobalErrorStatus.SEARCH_ERROR;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductSearchUseCase productSearchUseCase;

    /**
     * 상품 검색 수행
     */
    public ProductSearchResponse searchProducts(ProductSearchRequest request) {
        try {
            return productSearchUseCase.searchProducts(request);
        } catch (Exception e) {
            throw new RestApiException(SEARCH_ERROR);
        }
    }

    /**
     * 상품 검색 서비스 상태 확인
     */
    public String checkHealth() {
        return "상품 검색 서비스가 정상적으로 작동 중입니다.";
    }
}
