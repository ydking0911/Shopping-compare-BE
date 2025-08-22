package com.devmode.shop.domain.clickout.domain.service;

import com.devmode.shop.domain.clickout.domain.entity.PriceHistory;
import com.devmode.shop.domain.clickout.domain.repository.PriceHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PriceTrackingService 테스트")
class PriceTrackingServiceTest {

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @InjectMocks
    private PriceTrackingService priceTrackingService;

    private String productId;
    private String productTitle;
    private BigDecimal currentPrice;
    private String mallName;

    @BeforeEach
    void setUp() {
        productId = "test123";
        productTitle = "테스트 상품";
        currentPrice = new BigDecimal("12000.00");
        mallName = "테스트몰";
    }

    @Test
    @DisplayName("첫 번째 가격 기록 - 가격 변화 없음")
    void trackPrice_FirstRecord() {
        // given
        when(priceHistoryRepository.findTopByProductIdOrderByRecordedAtDesc(productId))
                .thenReturn(Optional.empty());

        PriceHistory savedHistory = PriceHistory.builder()
                .productId(productId)
                .productTitle(productTitle)
                .price(currentPrice)
                .source("CLICKOUT")
                .recordedAt(LocalDateTime.now())
                .mallName(mallName)
                .priceChange("STABLE")
                .priceChangeAmount(BigDecimal.ZERO)
                .build();

        when(priceHistoryRepository.save(any(PriceHistory.class))).thenReturn(savedHistory);

        // when
        priceTrackingService.trackPrice(productId, productTitle, currentPrice, mallName);

        // then
        verify(priceHistoryRepository, times(1)).findTopByProductIdOrderByRecordedAtDesc(productId);
        verify(priceHistoryRepository, times(1)).save(any(PriceHistory.class));
    }

    @Test
    @DisplayName("가격 상승 - UP 변화 기록")
    void trackPrice_PriceIncrease() {
        // given
        BigDecimal previousPrice = new BigDecimal("10000.00");
        BigDecimal expectedChangeAmount = new BigDecimal("2000.00");

        PriceHistory previousHistory = PriceHistory.builder()
                .productId(productId)
                .productTitle(productTitle)
                .price(previousPrice)
                .source("CLICKOUT")
                .recordedAt(LocalDateTime.now().minusHours(1))
                .mallName(mallName)
                .priceChange("STABLE")
                .priceChangeAmount(BigDecimal.ZERO)
                .build();

        when(priceHistoryRepository.findTopByProductIdOrderByRecordedAtDesc(productId))
                .thenReturn(Optional.of(previousHistory));

        PriceHistory savedHistory = PriceHistory.builder()
                .productId(productId)
                .productTitle(productTitle)
                .price(currentPrice)
                .source("CLICKOUT")
                .recordedAt(LocalDateTime.now())
                .mallName(mallName)
                .priceChange("UP")
                .priceChangeAmount(expectedChangeAmount)
                .build();

        when(priceHistoryRepository.save(any(PriceHistory.class))).thenReturn(savedHistory);

        // when
        priceTrackingService.trackPrice(productId, productTitle, currentPrice, mallName);

        // then
        verify(priceHistoryRepository, times(1)).findTopByProductIdOrderByRecordedAtDesc(productId);
        verify(priceHistoryRepository, times(1)).save(any(PriceHistory.class));
    }

    @Test
    @DisplayName("가격 하락 - DOWN 변화 기록")
    void trackPrice_PriceDecrease() {
        // given
        BigDecimal previousPrice = new BigDecimal("15000.00");
        BigDecimal currentPriceDown = new BigDecimal("12000.00");
        BigDecimal expectedChangeAmount = new BigDecimal("-3000.00");

        PriceHistory previousHistory = PriceHistory.builder()
                .productId(productId)
                .productTitle(productTitle)
                .price(previousPrice)
                .source("CLICKOUT")
                .recordedAt(LocalDateTime.now().minusHours(1))
                .mallName(mallName)
                .priceChange("STABLE")
                .priceChangeAmount(BigDecimal.ZERO)
                .build();

        when(priceHistoryRepository.findTopByProductIdOrderByRecordedAtDesc(productId))
                .thenReturn(Optional.of(previousHistory));

        PriceHistory savedHistory = PriceHistory.builder()
                .productId(productId)
                .productTitle(productTitle)
                .price(currentPriceDown)
                .source("CLICKOUT")
                .recordedAt(LocalDateTime.now())
                .mallName(mallName)
                .priceChange("DOWN")
                .priceChangeAmount(expectedChangeAmount)
                .build();

        when(priceHistoryRepository.save(any(PriceHistory.class))).thenReturn(savedHistory);

        // when
        priceTrackingService.trackPrice(productId, productTitle, currentPriceDown, mallName);

        // then
        verify(priceHistoryRepository, times(1)).findTopByProductIdOrderByRecordedAtDesc(productId);
        verify(priceHistoryRepository, times(1)).save(any(PriceHistory.class));
    }

    @Test
    @DisplayName("가격 동일 - STABLE 변화 기록")
    void trackPrice_PriceStable() {
        // given
        BigDecimal samePrice = new BigDecimal("12000.00");

        PriceHistory previousHistory = PriceHistory.builder()
                .productId(productId)
                .productTitle(productTitle)
                .price(samePrice)
                .source("CLICKOUT")
                .recordedAt(LocalDateTime.now().minusHours(1))
                .mallName(mallName)
                .priceChange("STABLE")
                .priceChangeAmount(BigDecimal.ZERO)
                .build();

        when(priceHistoryRepository.findTopByProductIdOrderByRecordedAtDesc(productId))
                .thenReturn(Optional.of(previousHistory));

        PriceHistory savedHistory = PriceHistory.builder()
                .productId(productId)
                .productTitle(productTitle)
                .price(currentPrice)
                .source("CLICKOUT")
                .recordedAt(LocalDateTime.now())
                .mallName(mallName)
                .priceChange("STABLE")
                .priceChangeAmount(BigDecimal.ZERO)
                .build();

        when(priceHistoryRepository.save(any(PriceHistory.class))).thenReturn(savedHistory);

        // when
        priceTrackingService.trackPrice(productId, productTitle, currentPrice, mallName);

        // then
        verify(priceHistoryRepository, times(1)).findTopByProductIdOrderByRecordedAtDesc(productId);
        verify(priceHistoryRepository, times(1)).save(any(PriceHistory.class));
    }

    @Test
    @DisplayName("null 몰명으로 가격 추적")
    void trackPrice_NullMallName() {
        // given
        when(priceHistoryRepository.findTopByProductIdOrderByRecordedAtDesc(productId))
                .thenReturn(Optional.empty());

        PriceHistory savedHistory = PriceHistory.builder()
                .productId(productId)
                .productTitle(productTitle)
                .price(currentPrice)
                .source("CLICKOUT")
                .recordedAt(LocalDateTime.now())
                .mallName(null)
                .priceChange("STABLE")
                .priceChangeAmount(BigDecimal.ZERO)
                .build();

        when(priceHistoryRepository.save(any(PriceHistory.class))).thenReturn(savedHistory);

        // when
        priceTrackingService.trackPrice(productId, productTitle, currentPrice, null);

        // then
        verify(priceHistoryRepository, times(1)).findTopByProductIdOrderByRecordedAtDesc(productId);
        verify(priceHistoryRepository, times(1)).save(any(PriceHistory.class));
    }
}
