package com.devmode.shop.domain.clickout.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PriceHistory 엔티티 테스트")
class PriceHistoryTest {

    @Test
    @DisplayName("PriceHistory 생성 테스트")
    void createPriceHistory() {
        // given
        String productId = "test123";
        String productTitle = "테스트 상품";
        BigDecimal price = new BigDecimal("10000.00");
        String source = "NAVER_SHOPPING";
        LocalDateTime recordedAt = LocalDateTime.now();
        String mallName = "테스트몰";
        Integer ranking = 1;
        String priceChange = "UP";
        BigDecimal priceChangeAmount = new BigDecimal("1000.00");

        // when
        PriceHistory priceHistory = PriceHistory.builder()
                .productId(productId)
                .productTitle(productTitle)
                .price(price)
                .source(source)
                .recordedAt(recordedAt)
                .mallName(mallName)
                .ranking(ranking)
                .priceChange(priceChange)
                .priceChangeAmount(priceChangeAmount)
                .build();

        // then
        assertThat(priceHistory.getProductId()).isEqualTo(productId);
        assertThat(priceHistory.getProductTitle()).isEqualTo(productTitle);
        assertThat(priceHistory.getPrice()).isEqualByComparingTo(price);
        assertThat(priceHistory.getSource()).isEqualTo(source);
        assertThat(priceHistory.getRecordedAt()).isEqualTo(recordedAt);
        assertThat(priceHistory.getMallName()).isEqualTo(mallName);
        assertThat(priceHistory.getRanking()).isEqualTo(ranking);
        assertThat(priceHistory.getPriceChange()).isEqualTo(priceChange);
        assertThat(priceHistory.getPriceChangeAmount()).isEqualByComparingTo(priceChangeAmount);
    }

    @Test
    @DisplayName("필수 필드만으로 PriceHistory 생성 테스트")
    void createPriceHistoryWithRequiredFieldsOnly() {
        // given
        String productId = "test123";
        String productTitle = "테스트 상품";
        BigDecimal price = new BigDecimal("10000.00");
        String source = "NAVER_SHOPPING";
        LocalDateTime recordedAt = LocalDateTime.now();

        // when
        PriceHistory priceHistory = PriceHistory.builder()
                .productId(productId)
                .productTitle(productTitle)
                .price(price)
                .source(source)
                .recordedAt(recordedAt)
                .build();

        // then
        assertThat(priceHistory.getProductId()).isEqualTo(productId);
        assertThat(priceHistory.getProductTitle()).isEqualTo(productTitle);
        assertThat(priceHistory.getPrice()).isEqualByComparingTo(price);
        assertThat(priceHistory.getSource()).isEqualTo(source);
        assertThat(priceHistory.getRecordedAt()).isEqualTo(recordedAt);
        
        // 선택적 필드들은 null이어야 함
        assertThat(priceHistory.getMallName()).isNull();
        assertThat(priceHistory.getRanking()).isNull();
        assertThat(priceHistory.getPriceChange()).isNull();
        assertThat(priceHistory.getPriceChangeAmount()).isNull();
    }

    @Test
    @DisplayName("가격 변화 계산 테스트")
    void calculatePriceChange() {
        // given
        BigDecimal oldPrice = new BigDecimal("10000.00");
        BigDecimal newPrice = new BigDecimal("12000.00");
        BigDecimal expectedChangeAmount = new BigDecimal("2000.00");

        // when
        PriceHistory priceHistory = PriceHistory.builder()
                .productId("test123")
                .productTitle("테스트 상품")
                .price(newPrice)
                .source("NAVER_SHOPPING")
                .recordedAt(LocalDateTime.now())
                .priceChange("UP")
                .priceChangeAmount(expectedChangeAmount)
                .build();

        // then
        assertThat(priceHistory.getPriceChange()).isEqualTo("UP");
        assertThat(priceHistory.getPriceChangeAmount()).isEqualByComparingTo(expectedChangeAmount);
    }
}
