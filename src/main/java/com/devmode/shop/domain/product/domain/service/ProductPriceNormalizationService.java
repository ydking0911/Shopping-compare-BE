package com.devmode.shop.domain.product.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

/**
 * 상품 가격 정규화 서비스
 * 가격, 할인율, 가격 표시 등의 정규화를 담당
 */
@Slf4j
@Service
public class ProductPriceNormalizationService {
    
    // 가격 추출을 위한 정규식
    private static final Pattern PRICE_PATTERN = Pattern.compile("([0-9,]+)원");
    
    // 할인율 추출을 위한 정규식
    private static final Pattern DISCOUNT_PATTERN = Pattern.compile("([0-9]+)%");
    
    /**
     * 가격 정규화
     */
    public BigDecimal normalizePrice(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        try {
            // 쉼표 제거 후 숫자만 추출
            String cleanPrice = priceStr.replaceAll("[^0-9]", "");
            
            if (cleanPrice.isEmpty()) {
                return BigDecimal.ZERO;
            }
            
            return new BigDecimal(cleanPrice);
            
        } catch (NumberFormatException e) {
            log.warn("가격 파싱 실패: {}", priceStr);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 할인율 계산
     */
    public BigDecimal calculateDiscountRate(BigDecimal price, BigDecimal originalPrice) {
        if (originalPrice == null || price == null || originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        try {
            return originalPrice.subtract(price)
                    .divide(originalPrice, 2, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
                    
        } catch (ArithmeticException e) {
            log.warn("할인율 계산 실패: price={}, originalPrice={}", price, originalPrice);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 가격 표시 텍스트 생성
     */
    public String generatePriceDisplay(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) == 0) {
            return "가격 정보 없음";
        }
        
        return String.format("%,d원", price.intValue());
    }
    
    /**
     * 가격 범위 표시 텍스트 생성
     */
    public String generatePriceRange(BigDecimal price, BigDecimal originalPrice) {
        if (price == null && originalPrice == null) {
            return "가격 정보 없음";
        }
        
        if (price == null) {
            return String.format("%,d원", originalPrice.intValue());
        }
        
        if (originalPrice == null) {
            return String.format("%,d원", price.intValue());
        }
        
        if (price.compareTo(originalPrice) == 0) {
            return String.format("%,d원", price.intValue());
        }
        
        return String.format("%,d원 ~ %,d원", price.intValue(), originalPrice.intValue());
    }
    
    /**
     * 가격대별 분류
     */
    public String classifyPriceRange(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) == 0) {
            return "가격 정보 없음";
        }
        
        int priceValue = price.intValue();
        
        if (priceValue < 10000) {
            return "1만원 미만";
        } else if (priceValue < 50000) {
            return "1만원-5만원";
        } else if (priceValue < 100000) {
            return "5만원-10만원";
        } else if (priceValue < 500000) {
            return "10만원-50만원";
        } else {
            return "50만원 이상";
        }
    }
}
