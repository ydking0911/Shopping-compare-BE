package com.devmode.shop.domain.clickout.domain.service;

import com.devmode.shop.domain.clickout.domain.entity.PriceHistory;
import com.devmode.shop.domain.clickout.domain.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PriceTrackingService {
    
    private static final String NAVER_SOURCE = "naver";
    
    private final PriceHistoryRepository priceHistoryRepository;
    
    @Transactional
    public void trackPrice(String productId, String productTitle, BigDecimal currentPrice, String mallName) {
        // 1. 이전 가격 조회
        Optional<PriceHistory> lastPrice = priceHistoryRepository
                .findTopByProductIdOrderByRecordedAtDesc(productId);
        
        // 2. 가격 변화 분석
        String priceChange = "STABLE";
        BigDecimal priceChangeAmount = BigDecimal.ZERO;
        
        if (lastPrice.isPresent()) {
            BigDecimal previousPrice = lastPrice.get().getPrice();
            int comparison = currentPrice.compareTo(previousPrice);
            
            if (comparison > 0) {
                priceChange = "UP";
                priceChangeAmount = currentPrice.subtract(previousPrice);
            } else if (comparison < 0) {
                priceChange = "DOWN";
                priceChangeAmount = previousPrice.subtract(currentPrice);
            }
        }
        
        // 3. 가격 히스토리 저장
        PriceHistory priceHistory = PriceHistory.builder()
                .productId(productId)
                .productTitle(productTitle)
                .price(currentPrice)
                .source(NAVER_SOURCE)
                .recordedAt(LocalDateTime.now())
                .mallName(mallName)
                .priceChange(priceChange)
                .priceChangeAmount(priceChangeAmount)
                .build();
        
        priceHistoryRepository.save(priceHistory);
    }
}
