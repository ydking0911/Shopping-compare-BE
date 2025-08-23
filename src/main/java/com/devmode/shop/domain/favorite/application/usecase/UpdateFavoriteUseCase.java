package com.devmode.shop.domain.favorite.application.usecase;

import com.devmode.shop.domain.favorite.application.dto.request.UpdateFavoriteRequest;
import com.devmode.shop.domain.favorite.application.dto.response.FavoriteResponse;
import com.devmode.shop.domain.favorite.domain.entity.Favorite;
import com.devmode.shop.domain.favorite.domain.repository.FavoriteRepository;
import com.devmode.shop.domain.product.application.dto.response.ProductItem;
import com.devmode.shop.domain.product.application.mapper.ProductMapper;
import com.devmode.shop.domain.product.domain.entity.Product;
import com.devmode.shop.domain.product.domain.repository.ProductRepository;
import com.devmode.shop.global.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.devmode.shop.global.exception.code.status.GlobalErrorStatus._NOT_FOUND;
import static com.devmode.shop.global.exception.code.status.GlobalErrorStatus._FORBIDDEN;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateFavoriteUseCase {
    
    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    public FavoriteResponse updateFavorite(String userId, Long favoriteId, UpdateFavoriteRequest request) {
        
        // 1. 즐겨찾기 존재 여부 및 권한 확인
        Favorite favorite = favoriteRepository.findById(favoriteId)
            .orElseThrow(() -> new RestApiException(_NOT_FOUND));
        
        if (!favorite.getUserId().equals(userId)) {
            throw new RestApiException(_FORBIDDEN);
        }
        
        // 2. 즐겨찾기 정보 업데이트
        if (request.memo() != null) {
            favorite.updateMemo(request.memo());
        }
        if (request.favoriteGroup() != null) {
            favorite.updateFavoriteGroup(request.favoriteGroup());
        }
        if (request.notificationEnabled() != null) {
            favorite.updateNotificationEnabled(request.notificationEnabled());
        }
        if (request.targetPrice() != null) {
            favorite.updateTargetPrice(request.targetPrice());
        }
        if (request.priority() != null) {
            favorite.updatePriority(request.priority());
        }
        
        Favorite updatedFavorite = favoriteRepository.save(favorite);
        
        // ProductItem 생성
        ProductItem productItem = createProductItem(updatedFavorite.getProduct().getId());
        
        return FavoriteResponse.from(updatedFavorite, productItem);
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
