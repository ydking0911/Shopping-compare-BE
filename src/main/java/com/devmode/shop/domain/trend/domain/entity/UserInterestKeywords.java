package com.devmode.shop.domain.trend.domain.entity;

import com.devmode.shop.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "user_interest_keywords")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInterestKeywords extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String keyword;
    
    @Column(nullable = false)
    private Integer priority; // 우선순위 (1이 가장 높음)
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt;
}
