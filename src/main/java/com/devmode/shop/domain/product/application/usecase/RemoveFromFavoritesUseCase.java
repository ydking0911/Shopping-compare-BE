package com.devmode.shop.domain.product.application.usecase;

import com.devmode.shop.domain.favorite.application.usecase.DeleteFavoriteUseCase;
import com.devmode.shop.domain.product.domain.repository.ProductRepository;
import com.devmode.shop.domain.product.domain.entity.Product;
import com.devmode.shop.global.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.devmode.shop.global.exception.code.status.GlobalErrorStatus._NOT_FOUND;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RemoveFromFavoritesUseCase {
    
    private final DeleteFavoriteUseCase deleteFavoriteUseCase;
    private final ProductRepository productRepository;
    
    public void removeFromFavorites(String userId, String productId) {
        log.info("즐겨찾기 제거 시작: userId={}, productId={}", userId, productId);
        
        try {
            // 1. 상품 존재 여부 확인
            Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new RestApiException(_NOT_FOUND));
            
            // 2. 즐겨찾기 제거
            deleteFavoriteUseCase.deleteFavoriteByProductId(userId, product.getId());
            
            log.info("즐겨찾기 제거 완료: userId={}, productId={}", userId, productId);
            
        } catch (RestApiException e) {
            log.error("즐겨찾기 제거 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("즐겨찾기 제거 중 오류 발생: {}", e.getMessage(), e);
            throw new RestApiException(_NOT_FOUND);
        }
    }
}
