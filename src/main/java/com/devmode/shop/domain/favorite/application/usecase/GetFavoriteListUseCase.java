package com.devmode.shop.domain.favorite.application.usecase;

import com.devmode.shop.domain.favorite.application.dto.request.FavoriteSearchRequest;
import com.devmode.shop.domain.favorite.application.dto.response.FavoriteListResponse;
import com.devmode.shop.domain.favorite.application.dto.response.FavoriteResponse;
import com.devmode.shop.domain.favorite.domain.entity.Favorite;
import com.devmode.shop.domain.favorite.domain.repository.FavoriteRepository;
import com.devmode.shop.domain.product.application.dto.response.ProductItem;
import com.devmode.shop.domain.product.application.mapper.ProductMapper;
import com.devmode.shop.domain.product.domain.entity.Product;
import com.devmode.shop.domain.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetFavoriteListUseCase {
    
    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    public FavoriteListResponse getFavoriteList(String userId, FavoriteSearchRequest request) {
        
        // 1. 페이징 정보 생성
        Pageable pageable = PageRequest.of(request.page() - 1, request.size());
        
        // 2. 검색 조건에 따른 즐겨찾기 조회
        List<Favorite> favorites;
        long totalElements;
        
        if (request.category() != null) {
            favorites = favoriteRepository.findByUserIdAndCategory(userId, request.category());
            totalElements = favorites.size();
        } else if (request.brand() != null) {
            favorites = favoriteRepository.findByUserIdAndBrand(userId, request.brand());
            totalElements = favorites.size();
        } else if (request.mallName() != null) {
            favorites = favoriteRepository.findByUserIdAndMallName(userId, request.mallName());
            totalElements = favorites.size();
        } else if (request.minPrice() != null || request.maxPrice() != null) {
            Integer minPrice = request.minPrice() != null ? request.minPrice() : 0;
            Integer maxPrice = request.maxPrice() != null ? request.maxPrice() : Integer.MAX_VALUE;
            favorites = favoriteRepository.findByUserIdAndPriceRange(userId, minPrice, maxPrice);
            totalElements = favorites.size();
        } else if (request.sortBy() != null) {
            favorites = getSortedFavorites(userId, request.sortBy());
            totalElements = favorites.size();
        } else {
            // 기본 페이징 조회
            Page<Favorite> favoritePage = favoriteRepository.findByUserIdAndIsActiveTrue(userId, pageable);
            favorites = favoritePage.getContent();
            totalElements = favoritePage.getTotalElements();
        }
        
        // 3. 페이징 적용 (수동으로)
        int startIndex = (request.page() - 1) * request.size();
        int endIndex = Math.min(startIndex + request.size(), favorites.size());
        List<Favorite> pagedFavorites = favorites.subList(startIndex, endIndex);
        
        // 4. DTO 변환
        List<FavoriteResponse> favoriteResponses = pagedFavorites.stream()
            .map(favorite -> {
                ProductItem productItem = createProductItem(favorite.getProduct().getId());
                return FavoriteResponse.from(favorite, productItem);
            })
            .collect(Collectors.toList());
        
        return FavoriteListResponse.from(favoriteResponses, request.page(), request.size(), totalElements);
    }
    
    private List<Favorite> getSortedFavorites(String userId, String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "price_asc" -> favoriteRepository.findByUserIdOrderByLowestPrice(userId);
            case "price_desc" -> favoriteRepository.findByUserIdOrderByHighestPrice(userId);
            case "rating" -> favoriteRepository.findByUserIdOrderByRating(userId);
            case "review_count" -> favoriteRepository.findByUserIdOrderByReviewCount(userId);
            case "priority" -> favoriteRepository.findByUserIdOrderByPriority(userId);
            default -> favoriteRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId);
        };
    }
    
    /**
     * 상품 ID로 ProductItem 생성
     */
    private ProductItem createProductItem(Long productId) {
        try {
            // 1. 상품 정보 조회
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: productId=" + productId));
            
            // 2. ProductMapper를 사용하여 ProductItem 생성
            ProductItem productItem = productMapper.toProductItem(product);
            
            log.info("ProductItem 생성 완료: productId={}", productId);
            return productItem;
            
        } catch (Exception e) {
            log.error("ProductItem 생성 실패: productId={}, error={}", productId, e.getMessage(), e);
            // 실패 시 기본 ProductItem 생성
            return createDefaultProductItem(productId);
        }
    }
    
    /**
     * 기본 ProductItem 생성 (상품 정보 조회 실패 시)
     */
    private ProductItem createDefaultProductItem(Long productId) {
        return ProductMapper.createDefaultProductItem(productId);
    }
}
