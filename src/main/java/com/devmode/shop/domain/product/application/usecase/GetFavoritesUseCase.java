package com.devmode.shop.domain.product.application.usecase;

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
public class GetFavoritesUseCase {
    
    private final GetFavoriteListUseCase getFavoriteListUseCase;
    
    public List<String> getFavorites(String userId) {
        log.info("즐겨찾기 목록 조회 시작: userId={}", userId);
        
        try {
            // 1. 사용자의 즐겨찾기 목록 조회 (기본 페이징)
            FavoriteSearchRequest searchRequest = new FavoriteSearchRequest(
                null, // category
                null, // brand
                null, // mallName
                null, // minPrice
                null, // maxPrice
                "created_at_desc", // sortBy
                1, // page
                100 // size (충분히 큰 수)
            );
            
            FavoriteListResponse favoriteList = getFavoriteListUseCase.getFavoriteList(userId, searchRequest);
            
            // 2. 상품 ID 목록 추출
            List<String> productIds = favoriteList.favorites().stream()
                .map(favorite -> favorite.product().id())
                .collect(Collectors.toList());
            
            log.info("즐겨찾기 목록 조회 완료: userId={}, 상품 수={}", userId, productIds.size());
            return productIds;
            
        } catch (Exception e) {
            log.error("즐겨찾기 목록 조회 실패: {}", e.getMessage(), e);
            return List.of();
        }
    }
}
