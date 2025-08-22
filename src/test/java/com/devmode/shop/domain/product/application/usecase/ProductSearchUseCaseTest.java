package com.devmode.shop.domain.product.application.usecase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devmode.shop.domain.product.application.dto.request.ProductSearchRequest;
import com.devmode.shop.domain.product.application.dto.response.NaverShoppingResponse;
import com.devmode.shop.domain.product.application.dto.response.ProductSearchResponse;
import com.devmode.shop.domain.product.domain.service.NaverApiQuotaService;
import com.devmode.shop.domain.product.domain.service.NaverShoppingApiService;
import com.devmode.shop.domain.product.domain.service.ProductCacheService;
import com.devmode.shop.domain.product.domain.service.ProductTransformService;

@ExtendWith(MockitoExtension.class)
class ProductSearchUseCaseTest {

    @Mock
    private NaverShoppingApiService naverShoppingApiService;

    @Mock
    private ProductCacheService productCacheService;

    @Mock
    private NaverApiQuotaService quotaService;

    @Mock
    private ProductTransformService transformService;

    @InjectMocks
    private ProductSearchUseCase productSearchUseCase;

    private ProductSearchRequest searchRequest;
    private NaverShoppingResponse naverResponse;
    private ProductSearchResponse expectedResponse;

    @BeforeEach
    void setUp() {
        searchRequest = ProductSearchRequest.of("노트북");

        naverResponse = new NaverShoppingResponse(
            "2024-01-01T00:00:00", 150, 1, 20, Arrays.asList()
        );

        expectedResponse = ProductSearchResponse.of("노트북", Arrays.asList());
    }

    @Test
    @DisplayName("캐시 히트 시 캐시된 결과 반환 테스트")
    void testSearchProducts_CacheHit() {
        // given
        when(productCacheService.isCached(searchRequest)).thenReturn(true);
        when(productCacheService.getCachedResult(searchRequest))
                .thenReturn(Optional.of(expectedResponse));

        // when
        ProductSearchResponse result = productSearchUseCase.searchProducts(searchRequest);

        // then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(productCacheService).isCached(searchRequest);
        verify(productCacheService).getCachedResult(searchRequest);
        verify(naverShoppingApiService, never()).searchProducts(any());
        verify(quotaService, never()).isQuotaExceeded();
    }

    @Test
    @DisplayName("캐시 미스 시 API 호출하여 결과 반환 테스트")
    void testSearchProducts_CacheMiss_Success() {
        // given
        when(productCacheService.isCached(searchRequest)).thenReturn(false);
        when(quotaService.isQuotaExceeded()).thenReturn(false);
        when(naverShoppingApiService.searchProducts(searchRequest)).thenReturn(naverResponse);
        when(quotaService.getCurrentDailyCount()).thenReturn(5);
        when(quotaService.getQuotaStatus()).thenReturn("NORMAL");
        when(transformService.transformToProductSearchResponse(
                eq(naverResponse), eq("노트북"), eq(1), eq(20), eq("sim"),
                anyList(), eq("fresh"), anyLong(), eq(5), eq("NORMAL")))
                .thenReturn(expectedResponse);

        // when
        ProductSearchResponse result = productSearchUseCase.searchProducts(searchRequest);

        // then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(productCacheService).isCached(searchRequest);
        verify(quotaService).isQuotaExceeded();
        verify(naverShoppingApiService).searchProducts(searchRequest);
        verify(quotaService).incrementApiCallCount();
        verify(transformService).transformToProductSearchResponse(
                eq(naverResponse), eq("노트북"), eq(1), eq(20), eq("sim"),
                anyList(), eq("fresh"), anyLong(), eq(5), eq("NORMAL"));
        verify(productCacheService).cacheSearchResult(searchRequest, expectedResponse);
    }

    @Test
    @DisplayName("API 쿼터 초과 시 예외 발생 테스트")
    void testSearchProducts_QuotaExceeded() {
        // given
        when(productCacheService.isCached(searchRequest)).thenReturn(false);
        when(quotaService.isQuotaExceeded()).thenReturn(true);

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productSearchUseCase.searchProducts(searchRequest));
        assertEquals("API quota exceeded for today", exception.getMessage());

        verify(productCacheService).isCached(searchRequest);
        verify(quotaService).isQuotaExceeded();
        verify(naverShoppingApiService, never()).searchProducts(any());
    }

    @Test
    @DisplayName("API 호출 실패 시 캐시 폴백 테스트")
    void testSearchProducts_ApiFailure_CacheFallback() {
        // given
        when(productCacheService.isCached(searchRequest)).thenReturn(false);
        when(quotaService.isQuotaExceeded()).thenReturn(false);
        when(naverShoppingApiService.searchProducts(searchRequest))
                .thenThrow(new RuntimeException("API 호출 실패"));
        when(productCacheService.getCachedResult(searchRequest))
                .thenReturn(Optional.of(expectedResponse));

        // when
        ProductSearchResponse result = productSearchUseCase.searchProducts(searchRequest);

        // then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(productCacheService).isCached(searchRequest);
        verify(quotaService).isQuotaExceeded();
        verify(naverShoppingApiService).searchProducts(searchRequest);
        verify(productCacheService).getCachedResult(searchRequest);
    }

    @Test
    @DisplayName("API 호출 실패 및 캐시 폴백도 실패 시 예외 발생 테스트")
    void testSearchProducts_ApiFailure_NoCacheFallback() {
        // given
        when(productCacheService.isCached(searchRequest)).thenReturn(false);
        when(quotaService.isQuotaExceeded()).thenReturn(false);
        when(naverShoppingApiService.searchProducts(searchRequest))
                .thenThrow(new RuntimeException("API 호출 실패"));
        when(productCacheService.getCachedResult(searchRequest))
                .thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productSearchUseCase.searchProducts(searchRequest));
        assertEquals("Product search failed and no fallback available", exception.getMessage());

        verify(productCacheService).isCached(searchRequest);
        verify(quotaService).isQuotaExceeded();
        verify(naverShoppingApiService).searchProducts(searchRequest);
        verify(productCacheService).getCachedResult(searchRequest);
    }

    @Test
    @DisplayName("필터 적용 로직 테스트")
    void testBuildAppliedFilters() {
        // given
        ProductSearchRequest requestWithFilters = new ProductSearchRequest(
            "테스트", 1, 20, "sim", Arrays.asList("used", "rental"), false,
            "전자제품", "컴퓨터", "노트북", "15인치", "삼성", "삼성전자",
            100000, 500000, 4.0, 100
        );

        when(productCacheService.isCached(requestWithFilters)).thenReturn(false);
        when(quotaService.isQuotaExceeded()).thenReturn(false);
        when(naverShoppingApiService.searchProducts(requestWithFilters)).thenReturn(naverResponse);
        when(quotaService.getCurrentDailyCount()).thenReturn(1);
        when(quotaService.getQuotaStatus()).thenReturn("NORMAL");
        when(transformService.transformToProductSearchResponse(
                eq(naverResponse), eq("테스트"), eq(1), eq(20), eq("sim"),
                anyList(), eq("fresh"), anyLong(), eq(1), eq("NORMAL")))
                .thenReturn(expectedResponse);

        // when
        ProductSearchResponse result = productSearchUseCase.searchProducts(requestWithFilters);

        // then
        assertNotNull(result);
        // 필터들이 올바르게 적용되는지 확인
        verify(transformService).transformToProductSearchResponse(
                eq(naverResponse), eq("테스트"), eq(1), eq(20), eq("sim"),
                argThat(filters -> filters.contains("used") && filters.contains("rental")),
                eq("fresh"), anyLong(), eq(1), eq("NORMAL"));
    }

    @Test
    @DisplayName("빈 필터 리스트 처리 테스트")
    void testBuildAppliedFilters_EmptyFilters() {
        // given - 빈 필터들을 포함한 요청 생성
        ProductSearchRequest requestWithEmptyFilters = new ProductSearchRequest(
            "테스트",           // keyword
            1,                 // page
            20,                // size
            "sim",             // sort
            new ArrayList<>(), // excludeFilters (빈 리스트)
            false,             // onlyNPay
            "",                // category1 (빈 문자열)
            "",                // category2 (빈 문자열)
            "",                // category3 (빈 문자열)
            "",                // category4 (빈 문자열)
            "",                // brand (빈 문자열)
            "",                // mallName (빈 문자열)
            0,                 // minPrice
            0,                 // maxPrice
            0.0,               // minRating
            0                  // minReviewCount
        );

        when(productCacheService.isCached(requestWithEmptyFilters)).thenReturn(false);
        when(quotaService.isQuotaExceeded()).thenReturn(false);
        when(naverShoppingApiService.searchProducts(requestWithEmptyFilters)).thenReturn(naverResponse);
        when(quotaService.getCurrentDailyCount()).thenReturn(1);
        when(quotaService.getQuotaStatus()).thenReturn("NORMAL");
        when(transformService.transformToProductSearchResponse(
                eq(naverResponse), eq("테스트"), eq(1), eq(20), eq("sim"),
                anyList(), eq("fresh"), anyLong(), eq(1), eq("NORMAL")))
                .thenReturn(expectedResponse);

        // when
        ProductSearchResponse result = productSearchUseCase.searchProducts(requestWithEmptyFilters);

        // then
        assertNotNull(result);
        // 빈 필터들이 올바르게 처리되는지 확인
        verify(transformService).transformToProductSearchResponse(
                eq(naverResponse), eq("테스트"), eq(1), eq(20), eq("sim"),
                argThat(filters -> filters.isEmpty()),
                eq("fresh"), anyLong(), eq(1), eq("NORMAL"));
    }

    @Test
    @DisplayName("null 필터 처리 테스트")
    void testBuildAppliedFilters_NullFilters() {
        // given - null 값들을 포함한 요청 생성
        ProductSearchRequest requestWithNullFilters = new ProductSearchRequest(
            "테스트",  // keyword
            1,         // page
            20,        // size
            "sim",     // sort
            null,      // excludeFilters
            null,      // onlyNPay
            null,      // category1
            null,      // category2
            null,      // category3
            null,      // category4
            null,      // brand
            null,      // mallName
            null,      // minPrice
            null,      // maxPrice
            null,      // minRating
            null       // minReviewCount
        );

        when(productCacheService.isCached(requestWithNullFilters)).thenReturn(false);
        when(quotaService.isQuotaExceeded()).thenReturn(false);
        when(naverShoppingApiService.searchProducts(requestWithNullFilters)).thenReturn(naverResponse);
        when(quotaService.getCurrentDailyCount()).thenReturn(1);
        when(quotaService.getQuotaStatus()).thenReturn("NORMAL");
        when(transformService.transformToProductSearchResponse(
                eq(naverResponse), eq("테스트"), eq(1), eq(20), eq("sim"),
                anyList(), eq("fresh"), anyLong(), eq(1), eq("NORMAL")))
                .thenReturn(expectedResponse);

        // when
        ProductSearchResponse result = productSearchUseCase.searchProducts(requestWithNullFilters);

        // then
        assertNotNull(result);
        // null 필터들이 무시되고 빈 리스트가 전달되는지 확인
        verify(transformService).transformToProductSearchResponse(
                eq(naverResponse), eq("테스트"), eq(1), eq(20), eq("sim"),
                argThat(filters -> filters.isEmpty()),
                eq("fresh"), anyLong(), eq(1), eq("NORMAL"));
    }
}
