package com.devmode.shop.domain.product.domain.service;

import com.devmode.shop.domain.product.application.dto.response.NaverProductItem;
import com.devmode.shop.domain.product.application.dto.response.ProductItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 상품 데이터 정규화 서비스
 * 네이버 API 응답을 표준화된 형태로 변환
 */
@Slf4j
@Service
public class ProductNormalizationService {
    
    // HTML 태그 제거를 위한 정규식
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    
    // 가격 추출을 위한 정규식
    private static final Pattern PRICE_PATTERN = Pattern.compile("([0-9,]+)원");
    
    // 할인율 추출을 위한 정규식
    private static final Pattern DISCOUNT_PATTERN = Pattern.compile("([0-9]+)%");
    
    /**
     * NaverProductItem을 정규화된 ProductItem으로 변환
     */
    public ProductItem normalizeProductItem(NaverProductItem naverItem, String searchKeyword, 
                                         List<String> appliedFilters, int searchRank) {
        try {
            // 가격 정보 정규화
            BigDecimal price = normalizePrice(naverItem.lprice());
            BigDecimal originalPrice = normalizePrice(naverItem.hprice());
            BigDecimal discountRate = calculateDiscountRate(price, originalPrice);
            
            // 제목 정규화
            String normalizedTitle = normalizeTitle(naverItem.title());
            String description = generateDescription(normalizedTitle);
            
            // 이미지 URL 정규화
            String imageUrl = normalizeImageUrl(naverItem.image());
            String thumbnailUrl = generateThumbnailUrl(imageUrl);
            
            // 판매자 정보 정규화
            String normalizedMallName = normalizeMallName(naverItem.mallName());
            String sellerType = determineSellerType(naverItem.productType(), appliedFilters);
            
            // 카테고리 정규화
            String normalizedCategory1 = normalizeCategory(naverItem.category1());
            String normalizedCategory2 = normalizeCategory(naverItem.category2());
            String normalizedCategory3 = normalizeCategory(naverItem.category3());
            String normalizedCategory4 = normalizeCategory(naverItem.category4());
            
            // 브랜드 정규화
            String normalizedBrand = normalizeBrand(naverItem.brand(), naverItem.maker());
            
            // 평점 및 리뷰 정규화
            BigDecimal rating = normalizeRating(naverItem.rating());
            Integer reviewCount = normalizeReviewCount(naverItem.reviewCount());
            
            return new ProductItem(
                naverItem.productId() != null ? naverItem.productId() : naverItem.naverProductId(),
                normalizedTitle,
                description,
                price,
                originalPrice,
                discountRate,
                generatePriceDisplay(price),
                generatePriceRange(price, originalPrice),
                imageUrl,
                thumbnailUrl,
                new ArrayList<>(), // 추가 이미지는 현재 지원하지 않음
                normalizedMallName,
                generateMallCode(normalizedMallName),
                sellerType,
                normalizedCategory1,
                normalizedCategory2,
                normalizedCategory3,
                normalizedCategory4,
                generateCategoryPath(normalizedCategory1, normalizedCategory2, normalizedCategory3, normalizedCategory4),
                normalizedBrand,
                generateBrandCode(normalizedBrand),
                naverItem.maker(),
                determineProductType(naverItem.productType()),
                determineCondition(naverItem.productType()),
                normalizeShippingInfo(naverItem.shippingInfo()),
                determineAvailability(),
                rating,
                reviewCount,
                generateRatingDisplay(rating),
                naverItem.link(),
                LocalDateTime.now(),
                "naver",
                searchKeyword,
                appliedFilters,
                searchRank
            );
            
        } catch (Exception e) {
            log.error("상품 정규화 중 오류 발생: {}", e.getMessage(), e);
            // 오류 발생 시 기본값으로 반환
            return createFallbackProductItem(naverItem, searchKeyword, appliedFilters, searchRank);
        }
    }
    
    /**
     * 가격 정규화 (원화 통일)
     */
    private BigDecimal normalizePrice(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 숫자와 쉼표만 추출
            String cleanPrice = priceStr.replaceAll("[^0-9,]", "");
            if (cleanPrice.isEmpty()) {
                return null;
            }
            
            // 쉼표 제거 후 BigDecimal로 변환
            return new BigDecimal(cleanPrice.replace(",", ""));
        } catch (NumberFormatException e) {
            log.warn("가격 파싱 실패: {}", priceStr);
            return null;
        }
    }
    
    /**
     * 할인율 계산
     */
    private BigDecimal calculateDiscountRate(BigDecimal price, BigDecimal originalPrice) {
        if (price == null || originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        if (price.compareTo(originalPrice) >= 0) {
            return BigDecimal.ZERO;
        }
        
        return originalPrice.subtract(price)
                .divide(originalPrice, 2, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }
    
    /**
     * 제목 정규화 (HTML 태그 제거, 길이 제한)
     */
    private String normalizeTitle(String title) {
        if (title == null) return "";
        
        // HTML 태그 제거
        String cleanTitle = HTML_TAG_PATTERN.matcher(title).replaceAll("");
        
        // 길이 제한 (100자)
        if (cleanTitle.length() > 100) {
            cleanTitle = cleanTitle.substring(0, 97) + "...";
        }
        
        return cleanTitle.trim();
    }
    
    /**
     * 상품 설명 생성
     */
    private String generateDescription(String title) {
        if (title == null || title.isEmpty()) return "";
        
        // 제목에서 주요 키워드 추출하여 간단한 설명 생성
        if (title.length() <= 50) {
            return title;
        }
        
        return title.substring(0, 50) + "...";
    }
    
    /**
     * 이미지 URL 정규화
     */
    private String normalizeImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }
        
        // HTTPS 강제 적용
        if (imageUrl.startsWith("http://")) {
            imageUrl = imageUrl.replace("http://", "https://");
        }
        
        return imageUrl.trim();
    }
    
    /**
     * 썸네일 URL 생성
     */
    private String generateThumbnailUrl(String imageUrl) {
        if (imageUrl == null) return null;
        
        // 네이버 이미지의 경우 썼네일 파라미터 추가
        if (imageUrl.contains("shopping.pstatic.net")) {
            return imageUrl + "?type=f80";
        }
        
        return imageUrl;
    }
    
    /**
     * 쇼핑몰명 정규화
     */
    private String normalizeMallName(String mallName) {
        if (mallName == null || mallName.trim().isEmpty()) {
            return "기타";
        }
        
        // 특수문자 제거 및 표준화
        String normalized = mallName.replaceAll("[^가-힣a-zA-Z0-9\\s]", "").trim();
        
        // 빈 문자열인 경우 기본값
        if (normalized.isEmpty()) {
            return "기타";
        }
        
        return normalized;
    }
    
    /**
     * 판매자 유형 결정
     */
    private String determineSellerType(String productType, List<String> appliedFilters) {
        if (appliedFilters != null) {
            if (appliedFilters.contains("used")) return "중고";
            if (appliedFilters.contains("rental")) return "렌탈";
            if (appliedFilters.contains("overseas")) return "해외직구";
        }
        
        if (productType != null) {
            if (productType.contains("중고")) return "중고";
            if (productType.contains("렌탈")) return "렌탈";
            if (productType.contains("해외")) return "해외직구";
        }
        
        return "신상품";
    }
    
    /**
     * 카테고리 정규화
     */
    private String normalizeCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return null;
        }
        
        return category.trim();
    }
    
    /**
     * 브랜드 정규화
     */
    private String normalizeBrand(String brand, String maker) {
        if (brand != null && !brand.trim().isEmpty()) {
            return brand.trim();
        }
        
        if (maker != null && !maker.trim().isEmpty()) {
            return maker.trim();
        }
        
        return null;
    }
    
    /**
     * 평점 정규화
     */
    private BigDecimal normalizeRating(String ratingStr) {
        if (ratingStr == null || ratingStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            BigDecimal rating = new BigDecimal(ratingStr);
            // 0.0 ~ 5.0 범위로 제한
            if (rating.compareTo(BigDecimal.ZERO) < 0) return BigDecimal.ZERO;
            if (rating.compareTo(new BigDecimal("5.0")) > 0) return new BigDecimal("5.0");
            return rating;
        } catch (NumberFormatException e) {
            log.warn("평점 파싱 실패: {}", ratingStr);
            return null;
        }
    }
    
    /**
     * 리뷰 수 정규화
     */
    private Integer normalizeReviewCount(String reviewCountStr) {
        if (reviewCountStr == null || reviewCountStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 숫자만 추출
            String cleanCount = reviewCountStr.replaceAll("[^0-9]", "");
            if (cleanCount.isEmpty()) {
                return null;
            }
            
            return Integer.parseInt(cleanCount);
        } catch (NumberFormatException e) {
            log.warn("리뷰 수 파싱 실패: {}", reviewCountStr);
            return null;
        }
    }
    
    /**
     * 가격 표시 텍스트 생성
     */
    private String generatePriceDisplay(BigDecimal price) {
        if (price == null) return "가격 정보 없음";
        return String.format("%,d원", price.intValue());
    }
    
    /**
     * 가격 범위 표시 텍스트 생성
     */
    private String generatePriceRange(BigDecimal price, BigDecimal originalPrice) {
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
    private String generateRatingDisplay(BigDecimal rating) {
        if (rating == null) return "평점 없음";
        return String.format("%.1f", rating);
    }
    
    /**
     * 카테고리 경로 생성
     */
    private String generateCategoryPath(String category1, String category2, String category3, String category4) {
        List<String> categories = new ArrayList<>();
        if (category1 != null) categories.add(category1);
        if (category2 != null) categories.add(category2);
        if (category3 != null) categories.add(category3);
        if (category4 != null) categories.add(category4);
        
        return categories.isEmpty() ? "" : String.join(" > ", categories);
    }
    
    /**
     * 쇼핑몰 코드 생성
     */
    private String generateMallCode(String mallName) {
        if (mallName == null) return "UNKNOWN";
        return mallName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
    }
    
    /**
     * 브랜드 코드 생성
     */
    private String generateBrandCode(String brand) {
        if (brand == null) return "UNKNOWN";
        return brand.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
    }
    
    /**
     * 상품 유형 결정
     */
    private String determineProductType(String productType) {
        if (productType == null) return "일반";
        
        if (productType.contains("중고")) return "중고품";
        if (productType.contains("렌탈")) return "렌탈품";
        if (productType.contains("해외")) return "해외직구";
        
        return "신상품";
    }
    
    /**
     * 상품 상태 결정
     */
    private String determineCondition(String productType) {
        if (productType == null) return "새상품";
        
        if (productType.contains("중고")) return "중고";
        if (productType.contains("리퍼")) return "리퍼";
        
        return "새상품";
    }
    
    /**
     * 배송 정보 정규화
     */
    private String normalizeShippingInfo(String shippingInfo) {
        if (shippingInfo == null || shippingInfo.trim().isEmpty()) {
            return "배송 정보 없음";
        }
        return shippingInfo.trim();
    }
    
    /**
     * 재고 상태 결정
     */
    private String determineAvailability() {
        return "재고 있음"; // 네이버 API에서는 재고 정보를 제공하지 않음
    }
    
    /**
     * 폴백 상품 아이템 생성 (오류 발생 시)
     */
    private ProductItem createFallbackProductItem(NaverProductItem naverItem, String searchKeyword, 
                                                List<String> appliedFilters, int searchRank) {
        return new ProductItem(
            naverItem.productId() != null ? naverItem.productId() : "unknown",
            naverItem.title() != null ? naverItem.title() : "상품명 없음",
            "상품 정보를 불러올 수 없습니다.",
            null, null, BigDecimal.ZERO, "가격 정보 없음", "가격 정보 없음",
            null, null, new ArrayList<>(),
            "기타", "UNKNOWN", "신상품",
            null, null, null, null, "",
            null, "UNKNOWN", null,
            "일반", "새상품", "배송 정보 없음", "재고 정보 없음",
            null, null, "평점 없음",
            naverItem.link(), LocalDateTime.now(), "error",
            searchKeyword, appliedFilters, searchRank
        );
    }
}
