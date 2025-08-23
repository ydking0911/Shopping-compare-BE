package com.devmode.shop.global.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "web-push")
public class WebPushProperties {
    
    /**
     * VAPID 공개 키
     */
    private String vapidPublicKey;
    
    /**
     * VAPID 개인 키
     */
    private String vapidPrivateKey;
    
    /**
     * VAPID 이메일 (VAPID 토큰 생성 시 사용)
     */
    private String vapidEmail;
    
    /**
     * Web Push 알림 TTL (초)
     */
    private Integer ttl = 86400; // 24시간
    
    /**
     * Web Push 알림 우선순위
     */
    private String priority = "normal"; // normal, high, very-high
    
    /**
     * Web Push 알림 아이콘 URL
     */
    private String iconUrl = "/icon-192x192.png";
    
    /**
     * Web Push 알림 배지 URL
     */
    private String badgeUrl = "/badge-72x72.png";
    
    /**
     * Web Push 알림 클릭 시 이동할 URL
     */
    private String clickActionUrl = "/";
}
