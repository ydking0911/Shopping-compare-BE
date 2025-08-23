package com.devmode.shop.domain.notification.domain.entity;

import com.devmode.shop.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "web_push_subscriptions")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebPushSubscription extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "endpoint", nullable = false, length = 1000)
    private String endpoint;
    
    @Column(name = "p256dh_key", nullable = false, length = 500)
    private String p256dhKey;
    
    @Column(name = "auth_secret", nullable = false, length = 500)
    private String authSecret;
    
    @Column(name = "browser_info", length = 200)
    private String browserInfo;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    // 편의 메서드
    public void updateSubscription(String endpoint, String p256dhKey, String authSecret) {
        this.endpoint = endpoint;
        this.p256dhKey = p256dhKey;
        this.authSecret = authSecret;
        this.lastUsedAt = LocalDateTime.now();
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public void activate() {
        this.isActive = true;
    }
    
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }
    
    public boolean isExpired() {
        return this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt);
    }
}
