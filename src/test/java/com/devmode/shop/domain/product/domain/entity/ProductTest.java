package com.devmode.shop.domain.product.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .title("테스트 상품")
                .link("https://test.com/product")
                .image("https://test.com/image.jpg")
                .lprice(new BigDecimal("10000"))
                .hprice(new BigDecimal("15000"))
                .mallName("테스트몰")
                .productId("TEST001")
                .productType("일반상품")
                .brand("테스트브랜드")
                .maker("테스트제조사")
                .category1("전자제품")
                .category2("컴퓨터")
                .category3("노트북")
                .category4("15인치")
                .searchKeyword("노트북")
                .naverProductId("NAVER001")
                .isUsed(false)
                .isRental(false)
                .isOverseas(false)
                .isNPay(true)
                .reviewCount(100)
                .rating(4.5)
                .shippingInfo("무료배송")
                .additionalInfo("추가정보")
                .build();
    }

    @Test
    @DisplayName("Product 빌더를 통한 정상 생성 테스트")
    void testProductBuilder() {
        // given & when
        Product builtProduct = Product.builder()
                .title("새 상품")
                .link("https://new.com/product")
                .image("https://new.com/image.jpg")
                .lprice(new BigDecimal("20000"))
                .hprice(new BigDecimal("25000"))
                .mallName("새몰")
                .productId("NEW001")
                .productType("신상품")
                .brand("새브랜드")
                .maker("새제조사")
                .category1("의류")
                .category2("상의")
                .category3("티셔츠")
                .category4("반팔")
                .searchKeyword("티셔츠")
                .naverProductId("NAVER002")
                .isUsed(false)
                .isRental(false)
                .isOverseas(false)
                .isNPay(false)
                .reviewCount(50)
                .rating(4.0)
                .shippingInfo("유료배송")
                .additionalInfo("신상품")
                .build();

        // then
        assertNotNull(builtProduct);
        assertEquals("새 상품", builtProduct.getTitle());
        assertEquals("https://new.com/product", builtProduct.getLink());
        assertEquals(new BigDecimal("20000"), builtProduct.getLprice());
        assertEquals(new BigDecimal("25000"), builtProduct.getHprice());
        assertEquals("새몰", builtProduct.getMallName());
        assertEquals("NEW001", builtProduct.getProductId());
        assertEquals("신상품", builtProduct.getProductType());
        assertEquals("새브랜드", builtProduct.getBrand());
        assertEquals("새제조사", builtProduct.getMaker());
        assertEquals("의류", builtProduct.getCategory1());
        assertEquals("상의", builtProduct.getCategory2());
        assertEquals("티셔츠", builtProduct.getCategory3());
        assertEquals("반팔", builtProduct.getCategory4());
        assertEquals("티셔츠", builtProduct.getSearchKeyword());
        assertEquals("NAVER002", builtProduct.getNaverProductId());
        assertFalse(builtProduct.getIsUsed());
        assertFalse(builtProduct.getIsRental());
        assertFalse(builtProduct.getIsOverseas());
        assertFalse(builtProduct.getIsNPay());
        assertEquals(50, builtProduct.getReviewCount());
        assertEquals(4.0, builtProduct.getRating());
        assertEquals("유료배송", builtProduct.getShippingInfo());
        assertEquals("신상품", builtProduct.getAdditionalInfo());
    }

    @Test
    @DisplayName("Product 기본값 테스트")
    void testProductDefaultValues() {
        // given & when
        Product defaultProduct = Product.builder()
                .title("기본 상품")
                .link("https://default.com/product")
                .image("https://default.com/image.jpg")
                .lprice(new BigDecimal("1000"))
                .hprice(new BigDecimal("2000"))
                .mallName("기본몰")
                .productId("DEFAULT001")
                .productType("기본상품")
                .brand("기본브랜드")
                .maker("기본제조사")
                .category1("기본카테고리")
                .category2("기본카테고리2")
                .category3("기본카테고리3")
                .category4("기본카테고리4")
                .searchKeyword("기본키워드")
                .naverProductId("NAVER_DEFAULT")
                .isUsed(false)
                .isRental(false)
                .isOverseas(false)
                .isNPay(false)
                .reviewCount(0)
                .rating(0.0)
                .shippingInfo("기본배송")
                .additionalInfo("기본정보")
                .build();

        // then
        assertNotNull(defaultProduct);
        assertEquals(0, defaultProduct.getReviewCount());
        assertEquals(0.0, defaultProduct.getRating());
        assertFalse(defaultProduct.getIsUsed());
        assertFalse(defaultProduct.getIsRental());
        assertFalse(defaultProduct.getIsOverseas());
        assertFalse(defaultProduct.getIsNPay());
    }

    @Test
    @DisplayName("Product 가격 비교 테스트")
    void testProductPriceComparison() {
        // given
        Product cheapProduct = Product.builder()
                .title("저렴한 상품")
                .lprice(new BigDecimal("5000"))
                .hprice(new BigDecimal("8000"))
                .build();

        Product expensiveProduct = Product.builder()
                .title("비싼 상품")
                .lprice(new BigDecimal("50000"))
                .hprice(new BigDecimal("80000"))
                .build();

        // when & then
        assertTrue(cheapProduct.getLprice().compareTo(expensiveProduct.getLprice()) < 0);
        assertTrue(cheapProduct.getHprice().compareTo(expensiveProduct.getHprice()) < 0);
        assertTrue(expensiveProduct.getLprice().compareTo(cheapProduct.getHprice()) > 0);
    }

    @Test
    @DisplayName("Product 카테고리 계층 구조 테스트")
    void testProductCategoryHierarchy() {
        // given & when
        String category1 = product.getCategory1();
        String category2 = product.getCategory2();
        String category3 = product.getCategory3();
        String category4 = product.getCategory4();

        // then
        assertEquals("전자제품", category1);
        assertEquals("컴퓨터", category2);
        assertEquals("노트북", category3);
        assertEquals("15인치", category4);
        
        assertNotNull(category1);
        assertNotNull(category2);
        assertNotNull(category3);
        assertNotNull(category4);
    }

    @Test
    @DisplayName("Product 검색 키워드 테스트")
    void testProductSearchKeyword() {
        // given & when
        String searchKeyword = product.getSearchKeyword();
        String title = product.getTitle();

        // then
        assertEquals("노트북", searchKeyword);
        assertEquals("테스트 상품", title);
        assertTrue(searchKeyword.length() > 0);
        assertTrue(title.length() > 0);
    }

    @Test
    @DisplayName("Product 리뷰 및 평점 테스트")
    void testProductReviewAndRating() {
        // given & when
        Integer reviewCount = product.getReviewCount();
        Double rating = product.getRating();

        // then
        assertEquals(100, reviewCount);
        assertEquals(4.5, rating);
        assertTrue(reviewCount >= 0);
        assertTrue(rating >= 0.0 && rating <= 5.0);
    }

    @Test
    @DisplayName("Product 배송 정보 테스트")
    void testProductShippingInfo() {
        // given & when
        String shippingInfo = product.getShippingInfo();
        String additionalInfo = product.getAdditionalInfo();

        // then
        assertEquals("무료배송", shippingInfo);
        assertEquals("추가정보", additionalInfo);
        assertNotNull(shippingInfo);
        assertNotNull(additionalInfo);
    }
}
