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
@Table(name = "price_histories")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistory extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String productId;           // 상품 ID
    
    @Column(nullable = false)
    private String productTitle;        // 상품명
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;           // 가격
    
    @Column(nullable = false)
    private String source;              // 가격 소스 (naver, coupang 등)
    
    @Column(nullable = false)
    private LocalDateTime recordedAt;   // 기록 시간
    
    @Column
    private String mallName;            // 쇼핑몰명
    
    @Column
    private Integer ranking;            // 검색 결과 순위
    
    @Column
    private String priceChange;         // 가격 변화 (UP, DOWN, STABLE)
    
    @Column(precision = 10, scale = 2)
    private BigDecimal priceChangeAmount; // 가격 변화량
}
