package com.devmode.shop.domain.product.domain.entity;

import com.devmode.shop.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "product_favorites")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFavorite extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 즐겨찾기한 사용자 ID
     */
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    /**
     * 즐겨찾기한 상품
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    /**
     * 즐겨찾기 메모 (사용자가 추가할 수 있는 개인 메모)
     */
    @Column(name = "memo", length = 500)
    private String memo;
    
    /**
     * 즐겨찾기 그룹 (사용자가 분류할 수 있는 그룹)
     */
    @Column(name = "favorite_group", length = 100)
    private String favoriteGroup;
    
    /**
     * 알림 설정 여부 (가격 변동, 재고 등)
     */
    @Column(name = "notification_enabled", nullable = false)
    @Builder.Default
    private Boolean notificationEnabled = false;
    
    /**
     * 목표 가격 (사용자가 설정한 목표 가격)
     */
    @Column(name = "target_price")
    private Integer targetPrice;
    
    /**
     * 즐겨찾기 우선순위 (1-5, 5가 가장 높음)
     */
    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 3;
    
    /**
     * 즐겨찾기 활성화 여부
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    // 편의 메서드
    public void updateMemo(String memo) {
        this.memo = memo;
    }
    
    public void updateFavoriteGroup(String favoriteGroup) {
        this.favoriteGroup = favoriteGroup;
    }
    
    public void updateNotificationEnabled(Boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }
    
    public void updateTargetPrice(Integer targetPrice) {
        this.targetPrice = targetPrice;
    }
    
    public void updatePriority(Integer priority) {
        if (priority != null && priority >= 1 && priority <= 5) {
            this.priority = priority;
        }
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public void activate() {
        this.isActive = true;
    }
    
    public boolean isTargetPriceReached() {
        if (this.targetPrice == null || this.product == null) {
            return false;
        }
        return this.product.getLprice().compareTo(BigDecimal.valueOf(this.targetPrice)) <= 0;
    }
}
