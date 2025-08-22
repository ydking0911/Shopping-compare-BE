package com.devmode.shop.domain.clickout.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductClickLog 엔티티 테스트")
class ProductClickLogTest {

    @Test
    @DisplayName("ProductClickLog 생성 테스트")
    void createProductClickLog() {
        // given
        String productId = "test123";
        String productTitle = "테스트 상품";
        String keyword = "테스트";
        String category = "전자제품";
        String brand = "테스트브랜드";
        BigDecimal price = new BigDecimal("10000.00");
        String mallName = "테스트몰";
        String userId = "user123";
        String sessionId = "session123";
        String userAgent = "Mozilla/5.0";
        String ipAddress = "127.0.0.1";
        LocalDateTime clickedAt = LocalDateTime.now();
        String referrer = "https://test.com";
        String searchFilters = "{\"category\": \"electronics\"}";

        // when
        ProductClickLog productClickLog = ProductClickLog.builder()
                .productId(productId)
                .productTitle(productTitle)
                .keyword(keyword)
                .category(category)
                .brand(brand)
                .price(price)
                .mallName(mallName)
                .userId(userId)
                .sessionId(sessionId)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .clickedAt(clickedAt)
                .referrer(referrer)
                .searchFilters(searchFilters)
                .build();

        // then
        assertThat(productClickLog.getProductId()).isEqualTo(productId);
        assertThat(productClickLog.getProductTitle()).isEqualTo(productTitle);
        assertThat(productClickLog.getKeyword()).isEqualTo(keyword);
        assertThat(productClickLog.getCategory()).isEqualTo(category);
        assertThat(productClickLog.getBrand()).isEqualTo(brand);
        assertThat(productClickLog.getPrice()).isEqualByComparingTo(price);
        assertThat(productClickLog.getMallName()).isEqualTo(mallName);
        assertThat(productClickLog.getUserId()).isEqualTo(userId);
        assertThat(productClickLog.getSessionId()).isEqualTo(sessionId);
        assertThat(productClickLog.getUserAgent()).isEqualTo(userAgent);
        assertThat(productClickLog.getIpAddress()).isEqualTo(ipAddress);
        assertThat(productClickLog.getClickedAt()).isEqualTo(clickedAt);
        assertThat(productClickLog.getReferrer()).isEqualTo(referrer);
        assertThat(productClickLog.getSearchFilters()).isEqualTo(searchFilters);
    }

    @Test
    @DisplayName("필수 필드만으로 ProductClickLog 생성 테스트")
    void createProductClickLogWithRequiredFieldsOnly() {
        // given
        String productId = "test123";
        String productTitle = "테스트 상품";
        String keyword = "테스트";
        BigDecimal price = new BigDecimal("10000.00");
        String sessionId = "session123";
        String userAgent = "Mozilla/5.0";
        String ipAddress = "127.0.0.1";
        LocalDateTime clickedAt = LocalDateTime.now();

        // when
        ProductClickLog productClickLog = ProductClickLog.builder()
                .productId(productId)
                .productTitle(productTitle)
                .keyword(keyword)
                .price(price)
                .sessionId(sessionId)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .clickedAt(clickedAt)
                .build();

        // then
        assertThat(productClickLog.getProductId()).isEqualTo(productId);
        assertThat(productClickLog.getProductTitle()).isEqualTo(productTitle);
        assertThat(productClickLog.getKeyword()).isEqualTo(keyword);
        assertThat(productClickLog.getPrice()).isEqualByComparingTo(price);
        assertThat(productClickLog.getSessionId()).isEqualTo(sessionId);
        assertThat(productClickLog.getUserAgent()).isEqualTo(userAgent);
        assertThat(productClickLog.getIpAddress()).isEqualTo(ipAddress);
        assertThat(productClickLog.getClickedAt()).isEqualTo(clickedAt);
        
        // 선택적 필드들은 null이어야 함
        assertThat(productClickLog.getCategory()).isNull();
        assertThat(productClickLog.getBrand()).isNull();
        assertThat(productClickLog.getMallName()).isNull();
        assertThat(productClickLog.getUserId()).isNull();
        assertThat(productClickLog.getReferrer()).isNull();
        assertThat(productClickLog.getSearchFilters()).isNull();
    }
}
