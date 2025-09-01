package com.devmode.shop.domain.product.application.usecase;

import com.devmode.shop.domain.favorite.application.usecase.DeleteFavoriteUseCase;
import com.devmode.shop.domain.product.domain.entity.Product;
import com.devmode.shop.domain.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import com.devmode.shop.global.exception.RestApiException;

@ExtendWith(MockitoExtension.class)
class RemoveFromFavoritesUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private DeleteFavoriteUseCase deleteFavoriteUseCase;

    @InjectMocks
    private RemoveFromFavoritesUseCase removeFromFavoritesUseCase;

    private Product testProduct;
    private String testUserId;
    private String testProductId;

    @BeforeEach
    void setUp() {
        testUserId = "test-user-123";
        testProductId = "12345";
        
        testProduct = Product.builder()
                .id(Long.valueOf(testProductId))
                .title("테스트 상품")
                .lprice(new BigDecimal("10000"))
                .hprice(new BigDecimal("15000"))
                .brand("테스트 브랜드")
                .category1("전자제품")
                .category2("스마트폰")
                .category3("갤럭시")
                .category4("S24")
                .mallName("테스트 몰")
                .productId(testProductId)
                .link("https://test.com/product/12345")
                .image("https://test.com/image/12345.jpg")
                .rating(4.5)
                .reviewCount(100)
                .build();
    }

    @Test
    @DisplayName("즐겨찾기에서 상품 제거 성공")
    void removeFromFavorites_Success() {
        // given
        when(productRepository.findByProductId(testProductId))
                .thenReturn(Optional.of(testProduct));
        doNothing().when(deleteFavoriteUseCase).deleteFavoriteByProductId(any(), any());

        // when
        removeFromFavoritesUseCase.removeFromFavorites(testUserId, testProductId);

        // then
        verify(productRepository).findByProductId(testProductId);
        verify(deleteFavoriteUseCase).deleteFavoriteByProductId(testUserId, Long.valueOf(testProductId));
    }

    @Test
    @DisplayName("상품을 찾을 수 없는 경우 예외 발생")
    void removeFromFavorites_ProductNotFound_ThrowsException() {
        // given
        when(productRepository.findByProductId(testProductId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> removeFromFavoritesUseCase.removeFromFavorites(testUserId, testProductId))
                .isInstanceOf(RestApiException.class);

        verify(productRepository).findByProductId(testProductId);
        verify(deleteFavoriteUseCase, never()).deleteFavoriteByProductId(any(), any());
    }
}
