package com.devmode.shop.domain.product.application.usecase;

import com.devmode.shop.domain.favorite.application.dto.response.FavoriteListResponse;
import com.devmode.shop.domain.favorite.application.dto.response.FavoriteResponse;
import com.devmode.shop.domain.favorite.application.usecase.GetFavoriteListUseCase;
import com.devmode.shop.domain.product.application.dto.request.ProductSearchRequest;
import com.devmode.shop.domain.product.application.dto.response.ProductItem;
import com.devmode.shop.domain.product.application.dto.response.ProductSearchResponse;
import com.devmode.shop.domain.product.application.dto.response.ProductSearchResponse.SearchMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchPersonalizedProductsUseCaseTest {

    @Mock
    private ProductSearchUseCase productSearchUseCase;

    @Mock
    private GetFavoriteListUseCase getFavoriteListUseCase;

    @InjectMocks
    private SearchPersonalizedProductsUseCase searchPersonalizedProductsUseCase;

    private String testUserId;
    private ProductSearchRequest testRequest;
    private List<FavoriteResponse> testFavorites;

    @BeforeEach
    void setUp() {
        testUserId = "test-user-123";
        
        testRequest = new ProductSearchRequest(
                "스마트폰", // keyword
                1, // page
                20, // size
                "sim", // sort
                null, // excludeFilters
                false, // onlyNPay
                null, // category1
                null, // category2
                null, // category3
                null, // category4
                null, // brand
                null, // mallName
                null, // minPrice
                null, // maxPrice
                null, // minRating
                null  // minReviewCount
        );

        ProductItem testProductItem = new ProductItem(
                "galaxy-s24", "갤럭시 S24", "삼성 갤럭시 S24",
                new BigDecimal("1200000"), new BigDecimal("1500000"), new BigDecimal("20.0"),
                "1,200,000원", "1,200,000원 ~ 1,500,000원",
                "https://samsung.com/image/s24.jpg", "https://samsung.com/thumb/s24.jpg", List.of(),
                "삼성전자", "SAMSUNG", "직판",
                "전자제품", "스마트폰", "갤럭시", "S24", "전자제품 > 스마트폰 > 갤럭시 > S24",
                "삼성", "SAMSUNG_BRAND", "삼성전자",
                "신상품", "새상품", "무료배송", "재고있음",
                new BigDecimal("4.8"), 500, "4.8",
                "https://samsung.com/galaxy-s24", LocalDateTime.now(), "naver",
                "갤럭시 S24", List.of(), 1
        );

        testFavorites = List.of(
                new FavoriteResponse(1L, testUserId, testProductItem, "갤럭시 S24",
                        "스마트폰", true, 1000000, 1, true, 
                        LocalDateTime.now(), LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("개인화된 상품 검색 성공")
    void searchPersonalizedProducts_Success() {
        // given
        FavoriteListResponse favoriteListResponse = new FavoriteListResponse(
                testFavorites, 1, 1, 1L, 1
        );
        
        SearchMetadata metadata = new SearchMetadata(
                "2024-08-24", "fresh", 100L, 1, "available"
        );
        
        ProductSearchResponse expectedResponse = new ProductSearchResponse(
                testRequest.keyword(), 1, 20, 1, 1, 1, 1,
                "sim", "naver", List.of(), List.of(), metadata
        );

        when(getFavoriteListUseCase.getFavoriteList(any(), any()))
                .thenReturn(favoriteListResponse);
        when(productSearchUseCase.searchProducts(any()))
                .thenReturn(expectedResponse);

        // when
        ProductSearchResponse result = searchPersonalizedProductsUseCase.searchPersonalizedProducts(testUserId, testRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedResponse);
        
        // getFavoriteList가 정확히 1번만 호출되는지 검증
        verify(getFavoriteListUseCase, org.mockito.Mockito.times(1)).getFavoriteList(any(), any());
        verify(productSearchUseCase).searchProducts(any());
    }

    @Test
    @DisplayName("즐겨찾기가 없는 경우 기본 검색 결과 반환")
    void searchPersonalizedProducts_NoFavorites_ReturnsDefaultSearch() {
        // given
        FavoriteListResponse emptyFavoriteList = new FavoriteListResponse(
                List.of(), 0, 1, 0L, 1
        );
        
        SearchMetadata metadata = new SearchMetadata(
                "2024-08-24", "fresh", 100L, 1, "available"
        );
        
        ProductSearchResponse expectedResponse = new ProductSearchResponse(
                testRequest.keyword(), 1, 20, 1, 1, 1, 1,
                "sim", "naver", List.of(), List.of(), metadata
        );

        when(getFavoriteListUseCase.getFavoriteList(any(), any()))
                .thenReturn(emptyFavoriteList);
        when(productSearchUseCase.searchProducts(any()))
                .thenReturn(expectedResponse);

        // when
        ProductSearchResponse result = searchPersonalizedProductsUseCase.searchPersonalizedProducts(testUserId, testRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedResponse);
        
        // getFavoriteList가 정확히 1번만 호출되는지 검증
        verify(getFavoriteListUseCase, org.mockito.Mockito.times(1)).getFavoriteList(any(), any());
        verify(productSearchUseCase).searchProducts(any());
    }

    @Test
    @DisplayName("예외 발생 시 기본 검색 결과 반환")
    void searchPersonalizedProducts_Exception_ReturnsDefaultSearch() {
        // given
        when(getFavoriteListUseCase.getFavoriteList(any(), any()))
                .thenThrow(new RuntimeException("테스트 예외"));
        
        SearchMetadata metadata = new SearchMetadata(
                "2024-08-24", "fresh", 100L, 1, "available"
        );
        
        ProductSearchResponse expectedResponse = new ProductSearchResponse(
                testRequest.keyword(), 1, 20, 1, 1, 1, 1,
                "sim", "naver", List.of(), List.of(), metadata
        );
        when(productSearchUseCase.searchProducts(any()))
                .thenReturn(expectedResponse);

        // when
        ProductSearchResponse result = searchPersonalizedProductsUseCase.searchPersonalizedProducts(testUserId, testRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedResponse);
        
        // getFavoriteList가 정확히 1번만 호출되는지 검증
        verify(getFavoriteListUseCase, org.mockito.Mockito.times(1)).getFavoriteList(any(), any());
        verify(productSearchUseCase).searchProducts(any());
    }
}
