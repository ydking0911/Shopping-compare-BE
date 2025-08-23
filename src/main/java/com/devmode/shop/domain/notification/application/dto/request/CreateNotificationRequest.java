package com.devmode.shop.domain.notification.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record CreateNotificationRequest(
    @NotNull(message = "사용자 ID는 필수입니다")
    String userId,
    
    @NotBlank(message = "FCM 토큰은 필수입니다")
    String fcmToken,
    
    @NotBlank(message = "알림 제목은 필수입니다")
    String title,
    
    @NotBlank(message = "알림 내용은 필수입니다")
    String body,
    
    @NotNull(message = "알림 타입은 필수입니다")
    String notificationType,
    
    Long productId,
    
    LocalDateTime scheduledAt
) {}
