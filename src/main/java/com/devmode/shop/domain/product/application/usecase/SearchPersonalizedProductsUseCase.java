package com.devmode.shop.domain.product.application.usecase;

import com.devmode.shop.domain.product.application.dto.request.ProductSearchRequest;
import com.devmode.shop.domain.product.application.dto.response.ProductSearchResponse;
import com.devmode.shop.domain.favorite.application.usecase.GetFavoriteListUseCase;
import com.devmode.shop.domain.favorite.application.dto.request.FavoriteSearchRequest;
import com.devmode.shop.domain.favorite.application.dto.response.FavoriteListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchPersonalizedProductsUseCase {
    
    private final ProductSearchUseCase productSearchUseCase;
    private final GetFavoriteListUseCase getFavoriteListUseCase;
    
    public ProductSearchResponse searchPersonalizedProducts(String userId, ProductSearchRequest request) {
        log.info("개인화된 상품 검색 시작: userId={}, keyword={}", userId, request.keyword());
        
        try {
            // 1. 사용자의 즐겨찾기 패턴 분석을 위한 요청 생성
            FavoriteSearchRequest favoriteRequest = new FavoriteSearchRequest(
                null, // category
                null, // brand
                null, // mallName
                null, // minPrice
                null, // maxPrice
                "created_at_desc", // sortBy
                1, // page
                20 // size
            );
            
            // 2. 사용자의 즐겨찾기 목록을 한 번만 가져와서 분석
            FavoriteListResponse favoriteList = getFavoriteListUseCase.getFavoriteList(userId, favoriteRequest);
            
            // 3. 즐겨찾기한 상품들의 키워드 추출 (최근 5개 상품의 제목)
            List<String> recentSearchKeywords = favoriteList.favorites().stream()
                    .map(favorite -> favorite.product().title())
                    .filter(title -> title != null)
                    .distinct()
                    .limit(5) // 최근 5개 상품의 키워드 추출
                    .collect(Collectors.toList());
            
            // 4. 즐겨찾기한 상품들의 카테고리와 브랜드 패턴 분석
            List<String> preferredCategories = favoriteList.favorites().stream()
                .map(favorite -> favorite.product().category1())
                .filter(category -> category != null)
                .distinct()
                .collect(Collectors.toList());
                
            List<String> preferredBrands = favoriteList.favorites().stream()
                .map(favorite -> favorite.product().brand())
                .filter(brand -> brand != null)
                .distinct()
                .collect(Collectors.toList());
            
            // 5. 개인화된 검색 요청 생성
            ProductSearchRequest personalizedRequest = new ProductSearchRequest(
                request.keyword(),
                request.page(),
                request.size(),
                request.sort(),
                request.excludeFilters(),
                request.onlyNPay(),
                preferredCategories.isEmpty() ? null : preferredCategories.get(0), // category1
                preferredCategories.size() > 1 ? preferredCategories.get(1) : null, // category2
                preferredCategories.size() > 2 ? preferredCategories.get(2) : null, // category3
                preferredCategories.size() > 3 ? preferredCategories.get(3) : null, // category4
                preferredBrands.isEmpty() ? null : preferredBrands.get(0), // brand
                request.mallName(),
                request.minPrice(),
                request.maxPrice(),
                request.minRating(), // minRating
                request.minReviewCount() // minReviewCount
            );
            
            // 6. 개인화된 검색 수행
            ProductSearchResponse response = productSearchUseCase.searchProducts(personalizedRequest);
            
            log.info("개인화된 검색 완료: userId={}, 선호 카테고리={}, 선호 브랜드={}, 결과 수={}", 
                    userId, preferredCategories.size(), preferredBrands.size(), response.products().size());
            
            return response;
            
        } catch (Exception e) {
            log.error("개인화된 검색 실패: {}", e.getMessage(), e);
            // 실패 시 기본 검색 결과 반환
            return productSearchUseCase.searchProducts(request);
        }
    }
}
