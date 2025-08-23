package com.devmode.shop.domain.notification.application.dto.response;

import com.devmode.shop.domain.notification.domain.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    String userId,
    String webPushEndpoint,
    String title,
    String body,
    String notificationType,
    Long productId,
    Boolean isRead,
    LocalDateTime scheduledAt,
    LocalDateTime sentAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getUserId(),
            notification.getWebPushEndpoint(),
            notification.getTitle(),
            notification.getBody(),
            notification.getNotificationType().name(),
            notification.getProductId(),
            notification.getIsRead(),
            notification.getScheduledAt(),
            notification.getSentAt(),
            notification.getCreatedAt(),
            notification.getUpdatedAt()
        );
    }
}
