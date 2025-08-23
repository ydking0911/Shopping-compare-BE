package com.devmode.shop.domain.notification.domain.service;

import com.devmode.shop.domain.favorite.domain.entity.Favorite;
import com.devmode.shop.domain.favorite.domain.repository.FavoriteRepository;
import com.devmode.shop.domain.notification.domain.entity.Notification;
import com.devmode.shop.domain.notification.domain.entity.WebPushSubscription;
import com.devmode.shop.domain.notification.domain.repository.NotificationRepository;
import com.devmode.shop.domain.notification.domain.repository.WebPushSubscriptionRepository;
import com.devmode.shop.global.config.properties.WebPushProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.devmode.shop.global.exception.RestApiException;
import com.devmode.shop.global.exception.code.status.GlobalErrorStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebPushNotificationService {
    
    private final WebPushSubscriptionRepository webPushSubscriptionRepository;
    private final NotificationRepository notificationRepository;
    private final FavoriteRepository favoriteRepository;
    private final WebPushProperties webPushProperties;
    private final RestTemplate restTemplate;
    
    /**
     * 가격 변동 알림 발송
     */
    @Async
    public void sendPriceChangeNotification(String productId, BigDecimal oldPrice, BigDecimal newPrice) {
        try {
            // 1. 해당 상품을 즐겨찾기한 사용자들 조회
            List<Favorite> favorites = favoriteRepository.findByProductIdAndNotificationEnabledTrueAndIsActiveTrue(Long.valueOf(productId));
            
            for (Favorite favorite : favorites) {
                // 2. 목표 가격 도달 여부 확인
                if (favorite.getTargetPrice() != null && 
                    newPrice.compareTo(BigDecimal.valueOf(favorite.getTargetPrice())) <= 0) {
                    
                    // 3. 목표 가격 도달 알림 발송
                    sendWebPushNotification(
                        favorite.getUserId(),
                        "🎯 목표 가격 도달!",
                        String.format("'%s' 상품이 목표 가격 %d원에 도달했습니다!", 
                            favorite.getProduct().getTitle(), favorite.getTargetPrice()),
                        Notification.NotificationType.TARGET_PRICE,
                        Long.valueOf(productId)
                    );
                }
                
                // 4. 일반 가격 변동 알림
                String priceChange = newPrice.compareTo(oldPrice) > 0 ? "상승" : "하락";
                sendWebPushNotification(
                    favorite.getUserId(),
                    "💰 가격 변동 알림",
                    String.format("'%s' 상품 가격이 %s했습니다. (%,d원 → %,d원)", 
                        favorite.getProduct().getTitle(), priceChange, oldPrice, newPrice),
                    Notification.NotificationType.PRICE_CHANGE,
                    Long.valueOf(productId)
                );
            }
        } catch (Exception e) {
            log.error("가격 변동 알림 발송 실패: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Web Push 알림 발송
     */
    private void sendWebPushNotification(String userId, String title, String body, 
                                      Notification.NotificationType type, Long productId) {
        try {
            // 1. 사용자의 활성화된 Web Push 구독들 조회
            List<WebPushSubscription> subscriptions = webPushSubscriptionRepository.findByUserIdAndIsActiveTrue(userId);
            
            if (subscriptions.isEmpty()) {
                log.warn("사용자 {}의 활성화된 Web Push 구독을 찾을 수 없습니다.", userId);
                return;
            }
            
            // 2. 각 구독에 알림 발송
            for (WebPushSubscription subscription : subscriptions) {
                try {
                    // Web Push API로 알림 발송
                    boolean success = sendWebPushToSubscription(subscription, title, body);
                    
                    if (success) {
                        log.info("Web Push 알림 발송 성공: userId={}, endpoint={}", userId, subscription.getEndpoint());
                        
                        // 구독 사용 시간 업데이트
                        subscription.updateLastUsed();
                        webPushSubscriptionRepository.save(subscription);
                        
                        // 알림 이력 저장
                        saveNotificationHistory(userId, subscription.getEndpoint(), title, body, type, productId);
                    } else {
                        log.warn("Web Push 알림 발송 실패: userId={}, endpoint={}", userId, subscription.getEndpoint());
                    }
                    
                } catch (Exception e) {
                    log.error("Web Push 알림 발송 실패: userId={}, endpoint={}, error={}", 
                        userId, subscription.getEndpoint(), e.getMessage());
                    
                    // 구독이 유효하지 않은 경우 비활성화
                    if (isInvalidSubscriptionError(e)) {
                        subscription.deactivate();
                        webPushSubscriptionRepository.save(subscription);
                        log.warn("유효하지 않은 Web Push 구독 비활성화: userId={}, endpoint={}", 
                            userId, subscription.getEndpoint());
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Web Push 알림 발송 실패: userId={}, error={}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * Web Push API를 통해 실제 알림 발송
     */
    private boolean sendWebPushToSubscription(WebPushSubscription subscription, String title, String body) {
        try {
            // 설정에서 VAPID 키 가져오기
            String vapidPublicKey = webPushProperties.getVapidPublicKey();
            String vapidPrivateKey = webPushProperties.getVapidPrivateKey();
            
            if (vapidPublicKey == null || vapidPrivateKey == null) {
                log.error("VAPID 키가 설정되지 않았습니다.");
                return false;
            }
            
            // 2. 페이로드 생성
            String payload = createWebPushPayload(title, body);
            
            // 3. HTTP POST 요청으로 알림 발송
            boolean success = sendWebPushRequest(subscription, payload, vapidPublicKey, vapidPrivateKey);
            
            if (success) {
                log.info("Web Push 알림 발송 성공: title={}, body={}, endpoint={}", 
                    title, body, subscription.getEndpoint());
            } else {
                log.warn("Web Push 알림 발송 실패: title={}, body={}, endpoint={}", 
                    title, body, subscription.getEndpoint());
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("Web Push API 호출 실패: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Web Push 페이로드 생성
     */
    private String createWebPushPayload(String title, String body) {
        // 설정에서 가져온 값들을 사용하여 페이로드 생성
        return String.format("""
            {
                "title": "%s",
                "body": "%s",
                "icon": "%s",
                "badge": "%s",
                "data": {
                    "url": "%s",
                    "timestamp": "%s"
                }
            }
            """, 
            title, 
            body, 
            webPushProperties.getIconUrl(),
            webPushProperties.getBadgeUrl(),
            webPushProperties.getClickActionUrl(),
            System.currentTimeMillis()
        );
    }
    
    /**
     * Web Push HTTP 요청 발송
     */
    private boolean sendWebPushRequest(WebPushSubscription subscription, String payload, 
                                     String vapidPublicKey, String vapidPrivateKey) {
        try {
            // 1. VAPID JWT 토큰 생성 (간단한 구현)
            String vapidSubject = "mailto:" + webPushProperties.getVapidEmail();
            
            // 2. HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "vapid t=" + vapidPublicKey + ", k=" + vapidPrivateKey);
            headers.set("TTL", String.valueOf(webPushProperties.getTtl()));
            headers.set("Urgency", webPushProperties.getPriority());
            
            // 3. HTTP POST 요청 발송
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                subscription.getEndpoint(), 
                entity, 
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            log.debug("Web Push 요청 결과: endpoint={}, status={}, success={}", 
                subscription.getEndpoint(), response.getStatusCode(), success);
            
            return success; // 임시로 성공 반환
            
        } catch (Exception e) {
            log.error("Web Push HTTP 요청 발송 실패: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Web Push 구독 오류 타입 확인
     */
    private boolean isInvalidSubscriptionError(Exception e) {
        String errorMessage = e.getMessage();
        return errorMessage != null && (
            errorMessage.contains("410") || // Gone
            errorMessage.contains("404") || // Not Found
            errorMessage.contains("403")    // Forbidden
        );
    }
    
    /**
     * 사용자의 Web Push 구독 등록/업데이트
     */
    @Transactional
    public void registerWebPushSubscription(String userId, String endpoint, String p256dhKey, 
                                         String authSecret, String browserInfo) {
        // 기존 구독이 있는지 확인
        Optional<WebPushSubscription> existingSubscription = webPushSubscriptionRepository.findByEndpoint(endpoint);
        
        if (existingSubscription.isPresent()) {
            // 기존 구독이 다른 사용자의 것인 경우
            if (!existingSubscription.get().getUserId().equals(userId)) {
                // 기존 구독 비활성화
                existingSubscription.get().deactivate();
                webPushSubscriptionRepository.save(existingSubscription.get());
            } else {
                // 기존 구독 업데이트
                existingSubscription.get().updateSubscription(endpoint, p256dhKey, authSecret);
                existingSubscription.get().updateLastUsed();
                webPushSubscriptionRepository.save(existingSubscription.get());
                return;
            }
        }
        
        // 새 구독 등록
        WebPushSubscription newSubscription = WebPushSubscription.builder()
            .userId(userId)
            .endpoint(endpoint)
            .p256dhKey(p256dhKey)
            .authSecret(authSecret)
            .browserInfo(browserInfo)
            .isActive(true)
            .lastUsedAt(LocalDateTime.now())
            .build();
        
        webPushSubscriptionRepository.save(newSubscription);
        log.info("Web Push 구독 등록 완료: userId={}, browserInfo={}", userId, browserInfo);
    }
    
    /**
     * Web Push 구독 삭제
     */
    @Transactional
    public void deleteWebPushSubscription(String userId, String endpoint) {
        Optional<WebPushSubscription> subscription = webPushSubscriptionRepository.findByEndpoint(endpoint);
        
        if (subscription.isEmpty()) {
            log.warn("삭제할 Web Push 구독을 찾을 수 없습니다: endpoint={}", endpoint);
            return;
        }
        
        WebPushSubscription webPushSubscription = subscription.get();
        
        // 다른 사용자의 구독을 삭제하려는 경우 권한 확인
        if (!webPushSubscription.getUserId().equals(userId)) {
            log.warn("사용자 {}가 다른 사용자의 Web Push 구독을 삭제하려고 시도: endpoint={}", userId, endpoint);
            throw new RestApiException(GlobalErrorStatus._FORBIDDEN);
        }
        
        // 구독 비활성화
        webPushSubscription.deactivate();
        webPushSubscriptionRepository.save(webPushSubscription);
        
        log.info("Web Push 구독 삭제 완료: userId={}, endpoint={}", userId, endpoint);
    }
    
    /**
     * 알림 이력 저장
     */
    @Transactional
    public void saveNotificationHistory(String userId, String webPushEndpoint, String title, String body,
                                     Notification.NotificationType type, Long productId) {
        Notification notification = Notification.builder()
            .userId(userId)
            .webPushEndpoint(webPushEndpoint)
            .title(title)
            .body(body)
            .notificationType(type)
            .productId(productId)
            .isRead(false)
            .sentAt(LocalDateTime.now())
            .build();
        
        notificationRepository.save(notification);
    }
    
    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markNotificationAsRead(Long notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RestApiException(GlobalErrorStatus._NOT_FOUND));
        
        if (!notification.getUserId().equals(userId)) {
            throw new RestApiException(GlobalErrorStatus._FORBIDDEN);
        }
        
        notification.markAsRead();
        notificationRepository.save(notification);
    }
}
