package com.devmode.shop.domain.product.domain.entity;

import com.devmode.shop.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String link;
    
    @Column(nullable = false)
    private String image;
    
    @Column(nullable = false)
    private BigDecimal lprice;
    
    @Column(nullable = false)
    private BigDecimal hprice;
    
    @Column(nullable = false)
    private String mallName;
    
    @Column(nullable = false)
    private String productId;
    
    @Column(nullable = false)
    private String productType;
    
    @Column(nullable = false)
    private String brand;
    
    @Column(nullable = false)
    private String maker;
    
    @Column(nullable = false)
    private String category1;
    
    @Column(nullable = false)
    private String category2;
    
    @Column(nullable = false)
    private String category3;
    
    @Column(nullable = false)
    private String category4;
    
    @Column(nullable = false)
    private String searchKeyword;
    
    @Column(nullable = false)
    private String naverProductId;
    
    @Column(nullable = false)
    private Boolean isUsed;
    
    @Column(nullable = false)
    private Boolean isRental;
    
    @Column(nullable = false)
    private Boolean isOverseas;
    
    @Column(nullable = false)
    private Boolean isNPay;
    
    @Column(nullable = false)
    private Integer reviewCount;
    
    @Column(nullable = false)
    private Double rating;
    
    @Column(nullable = false)
    private String shippingInfo;
    
    @Column(nullable = false)
    private String additionalInfo;
}
