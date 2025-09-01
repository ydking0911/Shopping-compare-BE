package com.devmode.shop.domain.clickout.domain.service;

import com.devmode.shop.domain.clickout.application.dto.request.ProductClickRequest;
import com.devmode.shop.domain.clickout.domain.entity.ProductClickLog;
import com.devmode.shop.domain.clickout.domain.repository.ProductClickLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClickoutLoggingService 테스트")
class ClickoutLoggingServiceTest {

    @Mock
    private ProductClickLogRepository productClickLogRepository;

    @Mock
    private PriceTrackingService priceTrackingService;

    @InjectMocks
    private ClickoutLoggingService clickoutLoggingService;

    private ProductClickRequest productClickRequest;

    @BeforeEach
    void setUp() {
        productClickRequest = new ProductClickRequest(
                "test123",
                "테스트 상품",
                "테스트",
                "전자제품",
                "테스트브랜드",
                new BigDecimal("10000.00"),
                "테스트몰",
                "user123",
                "session123",
                "Mozilla/5.0",
                "127.0.0.1",
                "https://test.com",
                "{\"category\": \"electronics\"}"
        );
    }

    @Test
    @DisplayName("상품 클릭 로깅 성공 테스트")
    void logProductClick_Success() {
        // given
        ProductClickLog savedLog = ProductClickLog.builder()
                .productId(productClickRequest.productId())
                .productTitle(productClickRequest.productTitle())
                .keyword(productClickRequest.keyword())
                .category(productClickRequest.category())
                .brand(productClickRequest.brand())
                .price(productClickRequest.price())
                .mallName(productClickRequest.mallName())
                .userId(productClickRequest.userId())
                .sessionId(productClickRequest.sessionId())
                .userAgent(productClickRequest.userAgent())
                .ipAddress(productClickRequest.ipAddress())
                .clickedAt(LocalDateTime.now())
                .referrer(productClickRequest.referrer())
                .searchFilters(productClickRequest.searchFilters())
                .build();

        when(productClickLogRepository.save(any(ProductClickLog.class))).thenReturn(savedLog);

        // when
        clickoutLoggingService.logProductClick(productClickRequest);

        // then
        verify(productClickLogRepository, times(1)).save(any(ProductClickLog.class));
        verify(priceTrackingService, times(1)).trackPrice(
                eq(productClickRequest.productId()),
                eq(productClickRequest.productTitle()),
                eq(productClickRequest.price()),
                eq(productClickRequest.mallName())
        );
    }

    @Test
    @DisplayName("비로그인 사용자 클릭 로깅 테스트")
    void logProductClick_AnonymousUser() {
        // given
        ProductClickRequest anonymousRequest = new ProductClickRequest(
                "test123",
                "테스트 상품",
                "테스트",
                null,
                null,
                new BigDecimal("10000.00"),
                null,
                null, // 비로그인 사용자
                "session123",
                "Mozilla/5.0",
                "127.0.0.1",
                null,
                null
        );

        ProductClickLog savedLog = ProductClickLog.builder()
                .productId(anonymousRequest.productId())
                .productTitle(anonymousRequest.productTitle())
                .keyword(anonymousRequest.keyword())
                .price(anonymousRequest.price())
                .sessionId(anonymousRequest.sessionId())
                .userAgent(anonymousRequest.userAgent())
                .ipAddress(anonymousRequest.ipAddress())
                .clickedAt(LocalDateTime.now())
                .build();

        when(productClickLogRepository.save(any(ProductClickLog.class))).thenReturn(savedLog);

        // when
        clickoutLoggingService.logProductClick(anonymousRequest);

        // then
        verify(productClickLogRepository, times(1)).save(any(ProductClickLog.class));
        verify(priceTrackingService, times(1)).trackPrice(
                eq(anonymousRequest.productId()),
                eq(anonymousRequest.productTitle()),
                eq(anonymousRequest.price()),
                eq(anonymousRequest.mallName())
        );
    }

    @Test
    @DisplayName("가격 추적 서비스 호출 실패 시에도 클릭 로깅은 성공해야 함")
    void logProductClick_PriceTrackingFails() {
        // given
        ProductClickLog savedLog = ProductClickLog.builder()
                .productId(productClickRequest.productId())
                .productTitle(productClickRequest.productTitle())
                .keyword(productClickRequest.keyword())
                .price(productClickRequest.price())
                .sessionId(productClickRequest.sessionId())
                .userAgent(productClickRequest.userAgent())
                .ipAddress(productClickRequest.ipAddress())
                .clickedAt(LocalDateTime.now())
                .build();

        when(productClickLogRepository.save(any(ProductClickLog.class))).thenReturn(savedLog);
        doThrow(new RuntimeException("가격 추적 실패")).when(priceTrackingService)
                .trackPrice(anyString(), anyString(), any(BigDecimal.class), anyString());

        // when & then
        // 가격 추적 실패해도 예외가 전파되지 않아야 함 (로깅은 성공)
        clickoutLoggingService.logProductClick(productClickRequest);

        verify(productClickLogRepository, times(1)).save(any(ProductClickLog.class));
        verify(priceTrackingService, times(1)).trackPrice(anyString(), anyString(), any(BigDecimal.class), anyString());
    }
}
