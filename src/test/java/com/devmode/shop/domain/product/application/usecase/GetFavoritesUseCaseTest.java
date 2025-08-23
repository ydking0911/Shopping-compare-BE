package com.devmode.shop.domain.product.application.usecase;

import com.devmode.shop.domain.favorite.application.dto.response.FavoriteResponse;
import com.devmode.shop.domain.favorite.application.dto.response.FavoriteListResponse;
import com.devmode.shop.domain.favorite.application.usecase.GetFavoriteListUseCase;
import com.devmode.shop.domain.product.application.dto.response.ProductItem;
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
class GetFavoritesUseCaseTest {

    @Mock
    private GetFavoriteListUseCase getFavoriteListUseCase;

    @InjectMocks
    private GetFavoritesUseCase getFavoritesUseCase;

    private String testUserId;
    private List<FavoriteResponse> testFavorites;

    @BeforeEach
    void setUp() {
        testUserId = "test-user-123";
        
        ProductItem testProductItem = new ProductItem(
                "1", "테스트 상품", "테스트 상품 설명",
                new BigDecimal("10000"), new BigDecimal("15000"), new BigDecimal("33.3"),
                "10,000원", "10,000원 ~ 15,000원",
                "https://test.com/image/12345.jpg", "https://test.com/thumb/12345.jpg", List.of(),
                "테스트 몰", "TEST_MALL", "직판",
                "전자제품", "스마트폰", "갤럭시", "S24", "전자제품 > 스마트폰 > 갤럭시 > S24",
                "테스트 브랜드", "TEST_BRAND", "테스트 제조사",
                "신상품", "새상품", "무료배송", "재고있음",
                new BigDecimal("4.5"), 100, "4.5",
                "https://test.com/product/12345", LocalDateTime.now(), "test",
                "테스트", List.of(), 1
        );

        // FavoriteResponse를 record의 생성자로 만들기 (Integer 타입 사용)
        testFavorites = List.of(
                new FavoriteResponse(1L, testUserId, testProductItem, "첫 번째 즐겨찾기", 
                        "전자제품", true, 8000, 1, true, 
                        LocalDateTime.now(), LocalDateTime.now()),
                new FavoriteResponse(2L, testUserId, testProductItem, "두 번째 즐겨찾기", 
                        "전자제품", false, null, 2, true, 
                        LocalDateTime.now(), LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("사용자의 즐겨찾기 상품 ID 목록 조회 성공")
    void getFavorites_Success() {
        // given
        when(getFavoriteListUseCase.getFavoriteList(any(), any()))
                .thenReturn(new FavoriteListResponse(testFavorites, 2, 1, 2L, 1));

        // when
        List<String> result = getFavoritesUseCase.getFavorites(testUserId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("1", "1");
        
        verify(getFavoriteListUseCase).getFavoriteList(any(), any());
    }

    @Test
    @DisplayName("즐겨찾기가 없는 경우 빈 리스트 반환")
    void getFavorites_EmptyList() {
        // given
        when(getFavoriteListUseCase.getFavoriteList(any(), any()))
                .thenReturn(new FavoriteListResponse(List.of(), 0, 1, 0L, 1));

        // when
        List<String> result = getFavoritesUseCase.getFavorites(testUserId);

        // then
        assertThat(result).isEmpty();
        
        verify(getFavoriteListUseCase).getFavoriteList(any(), any());
    }
}
