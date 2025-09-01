package com.devmode.shop.domain.notification.domain.service;

import com.devmode.shop.domain.favorite.domain.entity.Favorite;
import com.devmode.shop.domain.favorite.domain.repository.FavoriteRepository;
import com.devmode.shop.domain.product.domain.service.NaverShoppingApiService;
import com.devmode.shop.domain.product.application.dto.response.NaverShoppingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceChangeScheduler {
    
    private final WebPushNotificationService webPushNotificationService;
    private final FavoriteRepository favoriteRepository;
    private final NaverShoppingApiService naverShoppingApiService;
    
    /**
     * 매일 새벽 2시에 가격 변동 감지 및 알림 발송
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void checkPriceChanges() {
        log.info("가격 변동 감지 스케줄러 시작");
        
        try {
            // 1. 알림이 활성화된 즐겨찾기 상품들 조회
            List<Favorite> favorites = favoriteRepository.findByNotificationEnabledTrueAndIsActiveTrue();
            
            log.info("알림 활성화된 즐겨찾기 상품 수: {}", favorites.size());
            
            for (Favorite favorite : favorites) {
                try {
                    // 2. 네이버 API로 최신 가격 조회
                    BigDecimal currentPrice = getCurrentPrice(favorite.getProduct().getProductId());
                    
                    if (currentPrice == null) {
                        log.warn("상품 {}의 현재 가격을 조회할 수 없습니다.", favorite.getProduct().getTitle());
                        continue;
                    }
                    
                    // 3. 가격 변동 감지
                    BigDecimal oldPrice = favorite.getProduct().getLprice();
                    if (currentPrice != null && !currentPrice.equals(oldPrice)) {
                        log.info("가격 변동 감지: 상품={}, 이전가격={}, 현재가격={}", 
                            favorite.getProduct().getTitle(), oldPrice, currentPrice);
                        
                        // 4. 알림 발송
                        webPushNotificationService.sendPriceChangeNotification(
                            favorite.getProduct().getProductId(),
                            oldPrice,
                            currentPrice
                        );
                    }
                    
                } catch (Exception e) {
                    log.error("상품 {} 가격 변동 감지 실패: {}", 
                        favorite.getProduct().getTitle(), e.getMessage(), e);
                }
            }
            
            log.info("가격 변동 감지 스케줄러 완료");
            
        } catch (Exception e) {
            log.error("가격 변동 감지 스케줄러 실행 실패: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 상품의 현재 가격 조회 (네이버 API 서비스 사용)
     */
    private BigDecimal getCurrentPrice(String productId) {
        try {
            // 네이버 API를 통해 상품 정보 조회
            NaverShoppingResponse productInfo = naverShoppingApiService.searchProductById(productId);
            
            if (productInfo != null && productInfo.items() != null && !productInfo.items().isEmpty()) {
                // 첫 번째 상품의 최저가 반환
                String lprice = productInfo.items().get(0).lprice();
                return new BigDecimal(lprice);
            }
            
            log.warn("상품 ID {}에 대한 정보를 찾을 수 없습니다.", productId);
            return null;
            
        } catch (Exception e) {
            log.error("상품 ID {}의 가격 조회 중 오류 발생: {}", productId, e.getMessage(), e);
            return null;
        }
    }
}
