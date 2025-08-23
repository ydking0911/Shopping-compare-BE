package com.devmode.shop.domain.favorite.domain.entity;

import com.devmode.shop.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favorite_groups")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteGroup extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 그룹 소유자 ID
     */
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    /**
     * 그룹 이름
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    /**
     * 그룹 설명
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 그룹 색상 (HEX 코드)
     */
    @Column(name = "color", length = 7)
    private String color;
    
    /**
     * 그룹 순서
     */
    @Column(name = "sort_order")
    private Integer sortOrder;
    
    /**
     * 그룹 활성화 여부
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    // 편의 메서드
    public void updateName(String name) {
        this.name = name;
    }
    
    public void updateDescription(String description) {
        this.description = description;
    }
    
    public void updateColor(String color) {
        this.color = color;
    }
    
    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public void activate() {
        this.isActive = true;
    }
}
