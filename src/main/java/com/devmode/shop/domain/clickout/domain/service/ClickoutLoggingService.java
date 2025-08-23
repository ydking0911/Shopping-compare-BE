package com.devmode.shop.domain.clickout.domain.service;

import com.devmode.shop.domain.clickout.application.dto.request.ProductClickRequest;
import com.devmode.shop.domain.clickout.domain.entity.ProductClickLog;
import com.devmode.shop.domain.clickout.domain.repository.ProductClickLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClickoutLoggingService {
    
    private final ProductClickLogRepository clickLogRepository;
    private final PriceTrackingService priceTrackingService;
    
    @Transactional
    public void logProductClick(ProductClickRequest request) {
        // 1. 클릭 로그 저장
        ProductClickLog clickLog = ProductClickLog.builder()
                .productId(request.productId())
                .productTitle(request.productTitle())
                .keyword(request.keyword())
                .category(request.category())
                .brand(request.brand())
                .price(request.price())
                .mallName(request.mallName())
                .userId(request.userId())
                .sessionId(request.sessionId())
                .userAgent(request.userAgent())
                .ipAddress(request.ipAddress())
                .clickedAt(LocalDateTime.now())
                .referrer(request.referrer())
                .searchFilters(request.searchFilters())
                .build();
        
        clickLogRepository.save(clickLog);
        
        // 2. 가격 히스토리 추적 시작 (실패해도 클릭 로깅은 성공해야 함)
        try {
            priceTrackingService.trackPrice(
                    request.productId(), 
                    request.productTitle(), 
                    request.price(), 
                    request.mallName()
            );
        } catch (Exception e) {
            // 가격 추적 실패는 로그만 남기고 예외를 전파하지 않음
            // 실제 운영환경에서는 로깅 라이브러리 사용
            System.err.println("가격 추적 실패: " + e.getMessage());
        }
    }
}
