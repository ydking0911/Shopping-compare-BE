package com.devmode.shop.domain.clickout.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductClickRequest(
        @NotBlank(message = "상품 ID는 필수입니다.")
        String productId,
        
        @NotBlank(message = "상품명은 필수입니다.")
        String productTitle,
        
        @NotBlank(message = "검색 키워드는 필수입니다.")
        String keyword,
        
        String category,
        
        String brand,
        
        @NotNull(message = "가격은 필수입니다.")
        @Positive(message = "가격은 양수여야 합니다.")
        BigDecimal price,
        
        String mallName,
        
        String userId,  // null 가능 (비로그인 사용자)
        
        @NotBlank(message = "세션 ID는 필수입니다.")
        String sessionId,
        
        @NotBlank(message = "사용자 에이전트는 필수입니다.")
        String userAgent,
        
        @NotBlank(message = "IP 주소는 필수입니다.")
        String ipAddress,
        
        String referrer,
        
        String searchFilters  // JSON 형태의 검색 필터
) {}
