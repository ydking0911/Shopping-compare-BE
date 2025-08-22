package com.devmode.shop.domain.product.domain.entity;

import com.devmode.shop.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "search_histories")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchHistory extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String keyword;
    
    @Column(nullable = false)
    private String filters;
    
    @Column(nullable = false)
    private Integer page;
    
    @Column(nullable = false)
    private Integer size;
    
    @Column(nullable = false)
    private String sort;
    
    @Column(nullable = false)
    private Integer totalResults;
    
    @Column(nullable = false)
    private LocalDateTime lastSearchedAt;
    
    @Column(nullable = false)
    private String cacheKey;
    
    @Column(nullable = false)
    private LocalDateTime cacheExpiresAt;
    
    @Column(nullable = false)
    private Integer apiCallCount;
    
    @Column(nullable = false)
    private LocalDateTime lastApiCallAt;
}
