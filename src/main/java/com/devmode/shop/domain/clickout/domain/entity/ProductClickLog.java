package com.devmode.shop.domain.clickout.domain.entity;

import com.devmode.shop.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_click_logs")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductClickLog extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String productId;           // 네이버 상품 ID
    
    @Column(nullable = false)
    private String productTitle;        // 상품명
    
    @Column(nullable = false)
    private String keyword;             // 검색 키워드
    
    @Column
    private String category;            // 카테고리
    
    @Column
    private String brand;               // 브랜드
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price;           // 클릭 시점 가격
    
    @Column
    private String mallName;            // 쇼핑몰명
    
    @Column
    private String userId;              // 사용자 ID (null 가능)
    
    @Column(nullable = false)
    private String sessionId;           // 세션 ID
    
    @Column(nullable = false)
    private String userAgent;           // 사용자 에이전트
    
    @Column(nullable = false)
    private String ipAddress;           // IP 주소
    
    @Column(nullable = false)
    private LocalDateTime clickedAt;    // 클릭 시간
    
    @Column
    private String referrer;            // 이전 페이지 URL
    
    @Column(columnDefinition = "TEXT")
    private String searchFilters;       // 검색 필터 (JSON)
}
