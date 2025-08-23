package com.devmode.shop.domain.notification.domain.repository;

import com.devmode.shop.domain.notification.domain.entity.WebPushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebPushSubscriptionRepository extends JpaRepository<WebPushSubscription, Long> {
    
    /**
     * 사용자 ID로 활성화된 Web Push 구독 목록 조회
     */
    List<WebPushSubscription> findByUserIdAndIsActiveTrue(String userId);
    
    /**
     * endpoint로 Web Push 구독 조회
     */
    Optional<WebPushSubscription> findByEndpoint(String endpoint);
    
    /**
     * 사용자 ID와 endpoint로 Web Push 구독 조회
     */
    Optional<WebPushSubscription> findByUserIdAndEndpoint(String userId, String endpoint);
    
    /**
     * 사용자 ID로 Web Push 구독 개수 조회
     */
    long countByUserIdAndIsActiveTrue(String userId);
    
    /**
     * 만료된 Web Push 구독 조회
     */
    @Query("SELECT wps FROM WebPushSubscription wps WHERE wps.expiresAt IS NOT NULL AND wps.expiresAt < :now")
    List<WebPushSubscription> findExpiredSubscriptions(@Param("now") LocalDateTime now);
    
    /**
     * 장시간 사용되지 않은 Web Push 구독 조회 (30일 이상)
     */
    @Query("SELECT wps FROM WebPushSubscription wps WHERE wps.lastUsedAt < :threshold")
    List<WebPushSubscription> findInactiveSubscriptions(@Param("threshold") LocalDateTime threshold);
    
    /**
     * 특정 사용자의 모든 Web Push 구독 삭제
     */
    void deleteByUserId(String userId);
    
    /**
     * 특정 endpoint 삭제
     */
    void deleteByEndpoint(String endpoint);
}
