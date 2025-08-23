package com.devmode.shop.domain.product.application.usecase;

import com.devmode.shop.domain.favorite.application.usecase.CreateFavoriteUseCase;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.devmode.shop.global.exception.RestApiException;

@ExtendWith(MockitoExtension.class)
class AddToFavoritesUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CreateFavoriteUseCase createFavoriteUseCase;

    @InjectMocks
    private AddToFavoritesUseCase addToFavoritesUseCase;

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
    @DisplayName("즐겨찾기에 상품 추가 성공")
    void addToFavorites_Success() {
        // given
        when(productRepository.findByProductId(testProductId))
                .thenReturn(Optional.of(testProduct));
        when(createFavoriteUseCase.isAlreadyFavorited(testUserId, Long.valueOf(testProductId)))
                .thenReturn(false);
        when(createFavoriteUseCase.createFavorite(any())).thenReturn(null);

        // when
        addToFavoritesUseCase.addToFavorites(testUserId, testProductId);

        // then
        verify(productRepository).findByProductId(testProductId);
        verify(createFavoriteUseCase).isAlreadyFavorited(testUserId, Long.valueOf(testProductId));
        verify(createFavoriteUseCase).createFavorite(any());
    }

    @Test
    @DisplayName("상품을 찾을 수 없는 경우 예외 발생")
    void addToFavorites_ProductNotFound_ThrowsException() {
        // given
        when(productRepository.findByProductId(testProductId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> addToFavoritesUseCase.addToFavorites(testUserId, testProductId))
                .isInstanceOf(RestApiException.class);

        verify(productRepository).findByProductId(testProductId);
        verify(createFavoriteUseCase, never()).isAlreadyFavorited(any(), any());
        verify(createFavoriteUseCase, never()).createFavorite(any());
    }

    @Test
    @DisplayName("이미 즐겨찾기된 상품인 경우 조기 반환")
    void addToFavorites_AlreadyFavorited_EarlyReturn() {
        // given
        when(productRepository.findByProductId(testProductId))
                .thenReturn(Optional.of(testProduct));
        when(createFavoriteUseCase.isAlreadyFavorited(testUserId, Long.valueOf(testProductId)))
                .thenReturn(true);

        // when
        addToFavoritesUseCase.addToFavorites(testUserId, testProductId);

        // then
        verify(productRepository).findByProductId(testProductId);
        verify(createFavoriteUseCase).isAlreadyFavorited(testUserId, Long.valueOf(testProductId));
        verify(createFavoriteUseCase, never()).createFavorite(any());
    }
}
