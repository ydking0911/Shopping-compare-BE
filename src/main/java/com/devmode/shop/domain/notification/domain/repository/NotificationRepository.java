package com.devmode.shop.domain.notification.domain.repository;

import com.devmode.shop.domain.notification.domain.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 사용자 ID로 알림 목록 조회 (페이징)
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    /**
     * 사용자 ID로 읽지 않은 알림 개수 조회
     */
    long countByUserIdAndIsReadFalse(String userId);
    
    /**
     * 사용자 ID로 특정 타입의 알림 조회
     */
    List<Notification> findByUserIdAndNotificationType(String userId, Notification.NotificationType type);
    
    /**
     * 사용자 ID로 특정 상품의 알림 조회
     */
    List<Notification> findByUserIdAndProductId(String userId, Long productId);
    
    /**
     * 예약된 알림 조회 (발송 대기 중)
     */
    @Query("SELECT n FROM Notification n WHERE n.scheduledAt <= :now AND n.sentAt IS NULL")
    List<Notification> findScheduledNotifications(@Param("now") LocalDateTime now);
    
    /**
     * 특정 사용자의 모든 알림 삭제
     */
    void deleteByUserId(String userId);
    
    /**
     * 특정 상품의 모든 알림 삭제
     */
    void deleteByProductId(Long productId);
}
