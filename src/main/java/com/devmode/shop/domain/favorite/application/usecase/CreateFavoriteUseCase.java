package com.devmode.shop.domain.favorite.application.usecase;

import com.devmode.shop.domain.favorite.application.dto.request.CreateFavoriteRequest;
import com.devmode.shop.domain.favorite.application.dto.response.FavoriteResponse;
import com.devmode.shop.domain.favorite.domain.entity.Favorite;
import com.devmode.shop.domain.favorite.domain.repository.FavoriteRepository;
import com.devmode.shop.domain.product.application.mapper.ProductMapper;
import com.devmode.shop.domain.product.application.dto.response.ProductItem;
import com.devmode.shop.domain.product.domain.entity.Product;
import com.devmode.shop.domain.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CreateFavoriteUseCase {
    
    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    public FavoriteResponse createFavorite(CreateFavoriteRequest request) {
        log.info("즐겨찾기 생성 시작: userId={}, productId={}", request.userId(), request.productId());
        
        try {
            // 1. 이미 즐겨찾기된 상품인지 확인
            if (favoriteRepository.existsByUserIdAndProductIdAndIsActiveTrue(request.userId(), request.productId())) {
                log.warn("이미 즐겨찾기된 상품입니다: userId={}, productId={}", request.userId(), request.productId());
                throw new RuntimeException("이미 즐겨찾기된 상품입니다.");
            }
            
            // 2. 상품 엔티티 조회
            Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: productId=" + request.productId()));
            
            // 3. 즐겨찾기 엔티티 생성
            Favorite favorite = Favorite.builder()
                .userId(request.userId())
                .product(product)
                .memo(request.memo())
                .favoriteGroup(request.favoriteGroup())
                .notificationEnabled(request.notificationEnabled())
                .targetPrice(request.targetPrice())
                .priority(request.priority())
                .isActive(true)
                .build();
            
            // 3. 즐겨찾기 저장
            Favorite savedFavorite = favoriteRepository.save(favorite);
            
            // 4. ProductItem 생성
            ProductItem productItem = createProductItem(savedFavorite.getProduct().getId());
            
            log.info("즐겨찾기 생성 완료: favoriteId={}", savedFavorite.getId());
            
            return FavoriteResponse.from(savedFavorite, productItem);
            
        } catch (Exception e) {
            log.error("즐겨찾기 생성 실패: {}", e.getMessage(), e);
            throw e;
        }
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
    
    /**
     * 사용자가 이미 즐겨찾기한 상품인지 확인
     */
    public boolean isAlreadyFavorited(String userId, Long productId) {
        return favoriteRepository.existsByUserIdAndProductIdAndIsActiveTrue(userId, productId);
    }
}
