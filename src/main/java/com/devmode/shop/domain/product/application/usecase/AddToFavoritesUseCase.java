package com.devmode.shop.domain.product.application.usecase;

import com.devmode.shop.domain.favorite.application.usecase.CreateFavoriteUseCase;
import com.devmode.shop.domain.favorite.application.dto.request.CreateFavoriteRequest;
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
public class AddToFavoritesUseCase {
    
    private final CreateFavoriteUseCase createFavoriteUseCase;
    private final ProductRepository productRepository;
    
    public void addToFavorites(String userId, String productId) {
        log.info("즐겨찾기 추가 시작: userId={}, productId={}", userId, productId);
        
        try {
            // 1. 상품 존재 여부 확인
            Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new RestApiException(_NOT_FOUND));
            
            // 2. 이미 즐겨찾기된 상품인지 확인
            if (createFavoriteUseCase.isAlreadyFavorited(userId, product.getId())) {
                log.warn("사용자 {}가 이미 즐겨찾기한 상품입니다: {}", userId, productId);
                return; // 이미 즐겨찾기된 경우 조기 반환
            }
            
            // 3. 즐겨찾기 추가
            CreateFavoriteRequest request = new CreateFavoriteRequest(
                userId,
                product.getId(),
                null, // memo
                "기본", // favoriteGroup
                false, // notificationEnabled
                null, // targetPrice
                3 // priority
            );
            
            createFavoriteUseCase.createFavorite(request);
            
            log.info("즐겨찾기 추가 완료: userId={}, productId={}", userId, productId);
            
        } catch (RestApiException e) {
            log.error("즐겨찾기 추가 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("즐겨찾기 추가 중 오류 발생: {}", e.getMessage(), e);
            throw new RestApiException(_NOT_FOUND);
        }
    }
}
