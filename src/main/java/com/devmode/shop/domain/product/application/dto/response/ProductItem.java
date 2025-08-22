package com.devmode.shop.domain.product.application.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 정규화된 상품 데이터를 위한 DTO
 * 네이버 API 응답을 표준화된 형태로 변환
 */
public record ProductItem(
    // 기본 정보
    String id,                    // 상품 고유 ID
    String title,                 // 상품명 (HTML 태그 제거, 길이 제한)
    String description,           // 상품 설명 (간략화)
    
    // 가격 정보 (정규화)
    BigDecimal price,             // 현재 판매가 (원화)
    BigDecimal originalPrice,     // 원가/정가
    BigDecimal discountRate,      // 할인율 (%)
    String priceDisplay,          // 가격 표시 텍스트 (예: "12,900원")
    String priceRange,            // 가격 범위 (예: "12,900원 ~ 15,900원")
    
    // 이미지 정보 (정규화)
    String imageUrl,              // 메인 이미지 URL
    String thumbnailUrl,          // 썸네일 이미지 URL
    List<String> additionalImages, // 추가 이미지들
    
    // 판매자 정보 (정규화)
    String mallName,              // 쇼핑몰명 (표준화)
    String mallCode,              // 쇼핑몰 코드
    String sellerType,            // 판매자 유형 (직판/위탁/중고/렌탈/해외)
    
    // 카테고리 정보 (정규화)
    String category1,             // 대분류
    String category2,             // 중분류
    String category3,             // 소분류
    String category4,             // 세분류
    String categoryPath,          // 전체 카테고리 경로
    
    // 브랜드 정보 (정규화)
    String brand,                 // 브랜드명 (표준화)
    String brandCode,             // 브랜드 코드
    String maker,                 // 제조사
    
    // 상품 상태 및 정보
    String productType,           // 상품 유형 (신상품/중고품/렌탈품/해외직구)
    String condition,             // 상품 상태 (새상품/중고/리퍼)
    String shippingInfo,          // 배송 정보
    String availability,          // 재고 상태
    
    // 평점 및 리뷰 (정규화)
    BigDecimal rating,            // 평점 (0.0 ~ 5.0)
    Integer reviewCount,          // 리뷰 수
    String ratingDisplay,         // 평점 표시 (예: "4.5")
    
    // 추가 정보
    String productUrl,            // 상품 상세 페이지 URL
    LocalDateTime lastUpdated,    // 마지막 업데이트 시간
    String source,                // 데이터 출처 (naver, cached 등)
    
    // 메타데이터
    String searchKeyword,         // 검색 키워드
    List<String> appliedFilters,  // 적용된 필터들
    Integer searchRank            // 검색 결과 내 순위
) {
    
    /**
     * 할인율 계산
     */
    public BigDecimal calculateDiscountRate() {
        if (originalPrice == null || price == null || originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return originalPrice.subtract(price)
                .divide(originalPrice, 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }
    
    /**
     * 가격 표시 텍스트 생성
     */
    public String generatePriceDisplay() {
        if (price == null) return "가격 정보 없음";
        return String.format("%,d원", price.intValue());
    }
    
    /**
     * 가격 범위 표시 텍스트 생성
     */
    public String generatePriceRange() {
        if (price == null && originalPrice == null) return "가격 정보 없음";
        if (price == null) return String.format("%,d원", originalPrice.intValue());
        if (originalPrice == null) return String.format("%,d원", price.intValue());
        
        if (price.compareTo(originalPrice) == 0) {
            return String.format("%,d원", price.intValue());
        }
        return String.format("%,d원 ~ %,d원", price.intValue(), originalPrice.intValue());
    }
    
    /**
     * 평점 표시 텍스트 생성
     */
    public String generateRatingDisplay() {
        if (rating == null) return "평점 없음";
        return String.format("%.1f", rating);
    }
    
    /**
     * 카테고리 경로 생성
     */
    public String generateCategoryPath() {
        List<String> categories = new ArrayList<>();
        if (category1 != null) categories.add(category1);
        if (category2 != null) categories.add(category2);
        if (category3 != null) categories.add(category3);
        if (category4 != null) categories.add(category4);
        
        return categories.isEmpty() ? "" : String.join(" > ", categories);
    }
}
