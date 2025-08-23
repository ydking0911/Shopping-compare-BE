package com.devmode.shop.domain.product.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * 상품 텍스트 정규화 서비스
 * 제목, 브랜드, 카테고리 등의 텍스트 정규화를 담당
 */
@Slf4j
@Service
public class ProductTextNormalizationService {
    
    // HTML 태그 제거를 위한 정규식
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    
    /**
     * 제목 정규화
     */
    public String normalizeTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "상품명 없음";
        }
        
        // HTML 태그 제거
        String cleanTitle = HTML_TAG_PATTERN.matcher(title).replaceAll("");
        
        // 길이 제한 (100자)
        if (cleanTitle.length() > 100) {
            cleanTitle = cleanTitle.substring(0, 97) + "...";
        }
        
        return cleanTitle.trim();
    }
    
    /**
     * 브랜드 정규화
     */
    public String normalizeBrand(String brand, String maker) {
        if (brand != null && !brand.trim().isEmpty()) {
            return brand.trim();
        }
        
        if (maker != null && !maker.trim().isEmpty()) {
            return maker.trim();
        }
        
        return "기타";
    }
    
    /**
     * 카테고리 정규화
     */
    public String normalizeCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return "기타";
        }
        
        return category.trim();
    }
    
    /**
     * 쇼핑몰명 정규화
     */
    public String normalizeMallName(String mallName) {
        if (mallName == null || mallName.trim().isEmpty()) {
            return "기타";
        }
        
        return mallName.trim();
    }
    
    /**
     * 설명 생성
     */
    public String generateDescription(String title) {
        if (title == null || title.isEmpty()) {
            return "상품 정보를 불러올 수 없습니다.";
        }
        
        // 제목에서 설명 생성 (간단한 버전)
        if (title.length() > 50) {
            return title.substring(0, 50) + "...";
        }
        
        return title;
    }
    
    /**
     * 카테고리 경로 생성
     */
    public String generateCategoryPath(String category1, String category2, String category3, String category4) {
        StringBuilder path = new StringBuilder();
        
        if (category1 != null && !category1.isEmpty()) {
            path.append(category1);
        }
        
        if (category2 != null && !category2.isEmpty()) {
            if (path.length() > 0) path.append(" > ");
            path.append(category2);
        }
        
        if (category3 != null && !category3.isEmpty()) {
            if (path.length() > 0) path.append(" > ");
            path.append(category3);
        }
        
        if (category4 != null && !category4.isEmpty()) {
            if (path.length() > 0) path.append(" > ");
            path.append(category4);
        }
        
        return path.toString();
    }
}
