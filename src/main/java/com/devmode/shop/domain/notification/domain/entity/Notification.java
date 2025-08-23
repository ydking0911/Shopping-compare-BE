package com.devmode.shop.domain.notification.domain.entity;

import com.devmode.shop.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "web_push_endpoint", nullable = false)
    private String webPushEndpoint;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "body", nullable = false)
    private String body;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;
    
    @Column(name = "product_id")
    private Long productId;
    
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;
    
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    // 편의 메서드
    public void markAsRead() {
        this.isRead = true;
    }
    
    public void markAsUnread() {
        this.isRead = false;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    public boolean isScheduled() {
        return this.scheduledAt != null && this.sentAt == null;
    }
    
    public boolean isSent() {
        return this.sentAt != null;
    }
    
    public enum NotificationType {
        PRICE_CHANGE,      // 가격 변동
        TARGET_PRICE,      // 목표 가격 도달
        STOCK_ALERT,       // 재고 알림
        DISCOUNT_ALERT,    // 할인 알림
        GENERAL            // 일반 알림
    }
}
