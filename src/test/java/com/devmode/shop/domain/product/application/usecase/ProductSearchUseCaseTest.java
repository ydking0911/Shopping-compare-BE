package com.devmode.shop.domain.product.application.usecase;

import com.devmode.shop.domain.product.application.dto.request.ProductSearchRequest;
import com.devmode.shop.domain.product.application.dto.response.NaverShoppingResponse;
import com.devmode.shop.domain.product.application.dto.response.ProductSearchResponse;
import com.devmode.shop.domain.product.domain.service.NaverApiQuotaService;
import com.devmode.shop.domain.product.domain.service.NaverShoppingApiService;
import com.devmode.shop.domain.product.domain.service.ProductCacheService;
import com.devmode.shop.domain.product.domain.service.ProductTransformService;
import com.devmode.shop.global.exception.RestApiException;
import com.devmode.shop.global.exception.code.status.GlobalErrorStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductSearchUseCaseTest {

    @Mock
    private NaverShoppingApiService naverShoppingApiService;

    @Mock
    private ProductCacheService productCacheService;

    @Mock
    private NaverApiQuotaService quotaService;

    @Mock
    private ProductTransformService productTransformService;

    @InjectMocks
    private ProductSearchUseCase productSearchUseCase;

    private ProductSearchRequest request;
    private ProductSearchResponse mockResponse;
    private NaverShoppingResponse mockNaverResponse;

    @BeforeEach
    void setUp() {
        request = new ProductSearchRequest(
                "laptop",
                1,
                10,
                "sim",
                List.of("used"),
                false,
                "전자제품",
                "컴퓨터",
                "노트북",
                "",
                "",
                "",
                0,
                0,
                0.0,
                0
        );

        mockResponse = new ProductSearchResponse(
                "laptop",
                1,
                10,
                100,
                1,
                10,
                10,
                "sim",
                "fresh",
                List.of("used"),
                List.of(),
                new ProductSearchResponse.SearchMetadata(
                        "2025-08-23T00:00:00",
                        "fresh",
                        100L,
                        5,
                        "NORMAL"
                )
        );

        mockNaverResponse = new NaverShoppingResponse(
                "2025-08-23T00:00:00",
                100,
                1,
                10,
                List.of()
        );
    }

    @Test
    @DisplayName("캐시에서 상품 검색 결과를 가져올 수 있다")
    void searchProductsFromCache() {
        // given
        when(productCacheService.isCached(any())).thenReturn(true);
        when(productCacheService.getCachedResult(any())).thenReturn(Optional.of(mockResponse));

        // when
        ProductSearchResponse result = productSearchUseCase.searchProducts(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.keyword()).isEqualTo("laptop");
        verify(productCacheService).isCached(request);
        verify(productCacheService).getCachedResult(request);
        verify(naverShoppingApiService, never()).searchProducts(any());
    }

    @Test
    @DisplayName("캐시에 없을 때 네이버 API를 호출하여 상품을 검색할 수 있다")
    void searchProductsFromNaverApi() {
        // given
        when(productCacheService.isCached(any())).thenReturn(false);
        when(quotaService.isQuotaExceeded()).thenReturn(false);
        when(naverShoppingApiService.searchProducts(any())).thenReturn(mockNaverResponse);
        when(quotaService.getCurrentDailyCount()).thenReturn(5);
        when(quotaService.getQuotaStatus()).thenReturn("NORMAL");
        when(productTransformService.transformToProductSearchResponse(
                any(), anyString(), anyInt(), anyInt(), anyString(), anyList(), anyString(), anyLong(), anyInt(), anyString()
        )).thenReturn(mockResponse);

        // when
        ProductSearchResponse result = productSearchUseCase.searchProducts(request);

        // then
        assertThat(result).isNotNull();
        verify(productCacheService).isCached(request);
        verify(quotaService).isQuotaExceeded();
        verify(naverShoppingApiService).searchProducts(request);
        verify(quotaService).incrementApiCallCount();
        verify(productCacheService).cacheSearchResult(request, mockResponse);
    }

    @Test
    @DisplayName("API 쿼터 초과 시 예외를 던진다")
    void searchProductsThrowsExceptionWhenQuotaExceeded() {
        // given
        when(productCacheService.isCached(any())).thenReturn(false);
        when(quotaService.isQuotaExceeded()).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> productSearchUseCase.searchProducts(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("API quota exceeded for today");
    }

    @Test
    @DisplayName("네이버 API 호출 실패 시 예외를 던진다")
    void searchProductsThrowsExceptionWhenApiFails() {
        // given
        when(productCacheService.isCached(any())).thenReturn(false);
        when(quotaService.isQuotaExceeded()).thenReturn(false);
        when(naverShoppingApiService.searchProducts(any())).thenThrow(new RestApiException(GlobalErrorStatus._INTERNAL_SERVER_ERROR));
        when(productCacheService.getCachedResult(any())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productSearchUseCase.searchProducts(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product search failed and no fallback available");
    }

    @Test
    @DisplayName("카테고리 필터가 올바르게 적용된다")
    void searchProductsWithCategoryFilters() {
        // given
        ProductSearchRequest requestWithCategories = new ProductSearchRequest(
                "laptop",
                1,
                10,
                "sim",
                List.of(),
                false,
                "전자제품",
                "컴퓨터",
                "노트북",
                "",
                "",
                "",
                0,
                0,
                0.0,
                0
        );

        when(productCacheService.isCached(any())).thenReturn(false);
        when(quotaService.isQuotaExceeded()).thenReturn(false);
        when(naverShoppingApiService.searchProducts(any())).thenReturn(mockNaverResponse);
        when(quotaService.getCurrentDailyCount()).thenReturn(5);
        when(quotaService.getQuotaStatus()).thenReturn("NORMAL");
        when(productTransformService.transformToProductSearchResponse(
                any(), anyString(), anyInt(), anyInt(), anyString(), anyList(), anyString(), anyLong(), anyInt(), anyString()
        )).thenReturn(mockResponse);

        // when
        productSearchUseCase.searchProducts(requestWithCategories);

        // then
        verify(naverShoppingApiService).searchProducts(requestWithCategories);
    }

    @Test
    @DisplayName("페이지네이션이 올바르게 적용된다")
    void searchProductsWithPagination() {
        // given
        ProductSearchRequest requestWithPagination = new ProductSearchRequest(
                "laptop",
                2,
                20,
                "sim",
                List.of(),
                false,
                "",
                "",
                "",
                "",
                "",
                "",
                0,
                0,
                0.0,
                0
        );

        when(productCacheService.isCached(any())).thenReturn(false);
        when(quotaService.isQuotaExceeded()).thenReturn(false);
        when(naverShoppingApiService.searchProducts(any())).thenReturn(mockNaverResponse);
        when(quotaService.getCurrentDailyCount()).thenReturn(5);
        when(quotaService.getQuotaStatus()).thenReturn("NORMAL");
        when(productTransformService.transformToProductSearchResponse(
                any(), anyString(), anyInt(), anyInt(), anyString(), anyList(), anyString(), anyLong(), anyInt(), anyString()
        )).thenReturn(mockResponse);

        // when
        productSearchUseCase.searchProducts(requestWithPagination);

        // then
        verify(naverShoppingApiService).searchProducts(requestWithPagination);
    }
}
