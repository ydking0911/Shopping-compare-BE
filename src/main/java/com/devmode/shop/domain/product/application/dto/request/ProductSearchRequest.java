package com.devmode.shop.domain.product.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.util.List;
import java.util.Objects;

public record ProductSearchRequest(
    @NotBlank(message = "검색 키워드는 필수입니다.")
    String keyword,
    @Min(value = 1, message = "페이지는 1 이상이어야 합니다.")
    Integer page,
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.")
    Integer size,
    String sort, // sim: 정확도, date: 날짜, asc: 가격 오름차순, dsc: 가격 내림차순
    List<String> excludeFilters, // used, rental, overseas
    Boolean onlyNPay,
    String category1,
    String category2,
    String category3,
    String category4,
    String brand,
    String mallName,
    @Min(value = 0, message = "최소 가격은 0 이상이어야 합니다.")
    Integer minPrice,
    @Min(value = 0, message = "최대 가격은 0 이상이어야 합니다.")
    Integer maxPrice,
    @Min(value = 0, message = "최소 평점은 0 이상이어야 합니다.")
    @Max(value = 5, message = "최대 평점은 5 이하여야 합니다.")
    Double minRating,
    @Min(value = 0, message = "최소 리뷰 수는 0 이상이어야 합니다.")
    Integer minReviewCount
) {
    public ProductSearchRequest {
        // 기본값 설정
        page = (page != null && page > 0) ? page : 1;
        size = (size != null && size > 0 && size <= 100) ? size : 20;
        sort = (sort != null) ? sort : "sim";
        onlyNPay = (onlyNPay != null) ? onlyNPay : false;
        
        // null 리스트를 빈 리스트로 변환
        excludeFilters = (excludeFilters != null) ? excludeFilters : List.of();
        
        // 빈 문자열을 null로 변환
        category1 = (category1 != null && category1.trim().isEmpty()) ? null : category1;
        category2 = (category2 != null && category2.trim().isEmpty()) ? null : category2;
        category3 = (category3 != null && category3.trim().isEmpty()) ? null : category3;
        category4 = (category4 != null && category4.trim().isEmpty()) ? null : category4;
        brand = (brand != null && brand.trim().isEmpty()) ? null : brand;
        mallName = (mallName != null && mallName.trim().isEmpty()) ? null : mallName;
    }
    
    // 간단한 검색을 위한 정적 팩토리 메서드 (테스트용)
    public static ProductSearchRequest of(String keyword) {
        return new ProductSearchRequest(
            keyword, null, null, null, null, null,
            null, null, null, null, null, null,
            null, null, null, null
        );
    }
}
