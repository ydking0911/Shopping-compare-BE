package com.devmode.shop.domain.product.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

class ProductFavoriteTest {

    private Product product;
    private ProductFavorite productFavorite;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .title("테스트 상품")
                .lprice(new BigDecimal("10000"))
                .hprice(new BigDecimal("15000"))
                .build();

        productFavorite = ProductFavorite.builder()
                .userId("user123")
                .product(product)
                .memo("테스트 메모")
                .favoriteGroup("전자제품")
                .notificationEnabled(true)
                .targetPrice(12000)
                .priority(4)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("ProductFavorite 빌더를 통한 정상 생성 테스트")
    void testProductFavoriteBuilder() {
        // given & when
        ProductFavorite favorite = ProductFavorite.builder()
                .userId("user456")
                .product(product)
                .memo("새로운 메모")
                .favoriteGroup("의류")
                .notificationEnabled(false)
                .targetPrice(8000)
                .priority(2)
                .isActive(true)
                .build();

        // then
        assertNotNull(favorite);
        assertEquals("user456", favorite.getUserId());
        assertEquals(product, favorite.getProduct());
        assertEquals("새로운 메모", favorite.getMemo());
        assertEquals("의류", favorite.getFavoriteGroup());
        assertFalse(favorite.getNotificationEnabled());
        assertEquals(8000, favorite.getTargetPrice());
        assertEquals(2, favorite.getPriority());
        assertTrue(favorite.getIsActive());
    }

    @Test
    @DisplayName("ProductFavorite 기본값 테스트")
    void testProductFavoriteDefaultValues() {
        // given
        Product product = Product.builder()
                .title("테스트 상품")
                .lprice(new BigDecimal("100000"))
                .build();

        // when
        ProductFavorite favorite = ProductFavorite.builder()
                .userId("user123")
                .product(product)
                .build();

        // then
        assertNotNull(favorite);
        assertEquals("user123", favorite.getUserId());
        assertEquals(product, favorite.getProduct());
        
        // 기본값 확인
        assertNull(favorite.getMemo());
        assertNull(favorite.getFavoriteGroup());
        assertFalse(favorite.getNotificationEnabled()); // 기본값 false
        assertEquals(3, favorite.getPriority()); // 기본값 3
        assertTrue(favorite.getIsActive()); // 기본값 true
        assertNull(favorite.getTargetPrice());
    }

    @Test
    @DisplayName("메모 업데이트 테스트")
    void testUpdateMemo() {
        // given
        String newMemo = "업데이트된 메모";

        // when
        productFavorite.updateMemo(newMemo);

        // then
        assertEquals(newMemo, productFavorite.getMemo());
    }

    @Test
    @DisplayName("즐겨찾기 그룹 업데이트 테스트")
    void testUpdateFavoriteGroup() {
        // given
        String newGroup = "업데이트된 그룹";

        // when
        productFavorite.updateFavoriteGroup(newGroup);

        // then
        assertEquals(newGroup, productFavorite.getFavoriteGroup());
    }

    @Test
    @DisplayName("알림 설정 업데이트 테스트")
    void testUpdateNotificationEnabled() {
        // given
        Boolean newNotificationSetting = false;

        // when
        productFavorite.updateNotificationEnabled(newNotificationSetting);

        // then
        assertEquals(newNotificationSetting, productFavorite.getNotificationEnabled());
    }

    @Test
    @DisplayName("목표 가격 업데이트 테스트")
    void testUpdateTargetPrice() {
        // given
        Integer newTargetPrice = 9000;

        // when
        productFavorite.updateTargetPrice(newTargetPrice);

        // then
        assertEquals(newTargetPrice, productFavorite.getTargetPrice());
    }

    @Test
    @DisplayName("우선순위 업데이트 테스트 - 유효한 범위")
    void testUpdatePriorityWithValidRange() {
        // given
        Integer validPriority = 5;

        // when
        productFavorite.updatePriority(validPriority);

        // then
        assertEquals(validPriority, productFavorite.getPriority());
    }

    @Test
    @DisplayName("우선순위 업데이트 테스트 - 범위 밖 값")
    void testUpdatePriorityWithInvalidRange() {
        // given
        Integer invalidPriority = 6;
        Integer originalPriority = productFavorite.getPriority();

        // when
        productFavorite.updatePriority(invalidPriority);

        // then
        assertEquals(originalPriority, productFavorite.getPriority()); // 변경되지 않아야 함
    }

    @Test
    @DisplayName("우선순위 업데이트 테스트 - null 값")
    void testUpdatePriorityWithNull() {
        // given
        Integer originalPriority = productFavorite.getPriority();

        // when
        productFavorite.updatePriority(null);

        // then
        assertEquals(originalPriority, productFavorite.getPriority()); // 변경되지 않아야 함
    }

    @Test
    @DisplayName("즐겨찾기 비활성화 테스트")
    void testDeactivate() {
        // when
        productFavorite.deactivate();

        // then
        assertFalse(productFavorite.getIsActive());
    }

    @Test
    @DisplayName("즐겨찾기 활성화 테스트")
    void testActivate() {
        // given
        productFavorite.deactivate();
        assertFalse(productFavorite.getIsActive());

        // when
        productFavorite.activate();

        // then
        assertTrue(productFavorite.getIsActive());
    }

    @Test
    @DisplayName("목표 가격 도달 확인 테스트 - 도달한 경우")
    void testIsTargetPriceReached_Reached() {
        // given
        Product cheapProduct = Product.builder()
                .lprice(new BigDecimal("8000"))
                .build();
        
        ProductFavorite favorite = ProductFavorite.builder()
                .product(cheapProduct)
                .targetPrice(10000)
                .build();

        // when & then
        assertTrue(favorite.isTargetPriceReached());
    }

    @Test
    @DisplayName("목표 가격 도달 확인 테스트 - 도달하지 않은 경우")
    void testIsTargetPriceReached_NotReached() {
        // given
        Product expensiveProduct = Product.builder()
                .lprice(new BigDecimal("15000"))
                .build();
        
        ProductFavorite favorite = ProductFavorite.builder()
                .product(expensiveProduct)
                .targetPrice(10000)
                .build();

        // when & then
        assertFalse(favorite.isTargetPriceReached());
    }

    @Test
    @DisplayName("목표 가격 도달 확인 테스트 - 목표 가격이 null인 경우")
    void testIsTargetPriceReached_NullTargetPrice() {
        // given
        ProductFavorite favorite = ProductFavorite.builder()
                .product(product)
                .targetPrice(null)
                .build();

        // when & then
        assertFalse(favorite.isTargetPriceReached());
    }

    @Test
    @DisplayName("목표 가격 도달 확인 테스트 - 상품이 null인 경우")
    void testIsTargetPriceReached_NullProduct() {
        // given
        ProductFavorite favorite = ProductFavorite.builder()
                .product(null)
                .targetPrice(10000)
                .build();

        // when & then
        assertFalse(favorite.isTargetPriceReached());
    }

    @Test
    @DisplayName("목표 가격 도달 확인 테스트 - 정확한 가격 일치")
    void testIsTargetPriceReached_ExactMatch() {
        // given
        Product exactProduct = Product.builder()
                .lprice(new BigDecimal("10000"))
                .build();
        
        ProductFavorite favorite = ProductFavorite.builder()
                .product(exactProduct)
                .targetPrice(10000)
                .build();

        // when & then
        assertTrue(favorite.isTargetPriceReached());
    }
}
