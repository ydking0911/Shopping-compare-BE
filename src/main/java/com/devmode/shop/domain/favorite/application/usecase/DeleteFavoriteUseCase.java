package com.devmode.shop.domain.favorite.application.usecase;

import com.devmode.shop.domain.favorite.domain.entity.Favorite;
import com.devmode.shop.domain.favorite.domain.repository.FavoriteRepository;
import com.devmode.shop.global.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.devmode.shop.global.exception.code.status.GlobalErrorStatus._NOT_FOUND;
import static com.devmode.shop.global.exception.code.status.GlobalErrorStatus._FORBIDDEN;

@Service
@Transactional
@RequiredArgsConstructor
public class DeleteFavoriteUseCase {
    
    private final FavoriteRepository favoriteRepository;
    
    public void deleteFavorite(String userId, Long favoriteId) {
        
        // 1. 즐겨찾기 존재 여부 및 권한 확인
        Favorite favorite = favoriteRepository.findById(favoriteId)
            .orElseThrow(() -> new RestApiException(_NOT_FOUND));
        
        if (!favorite.getUserId().equals(userId)) {
            throw new RestApiException(_FORBIDDEN);
        }
        
        // 2. 소프트 삭제 (isActive = false)
        favorite.deactivate();
        favoriteRepository.save(favorite);
    }
    
    public void deleteFavoriteByProductId(String userId, Long productId) {
        
        // 1. 사용자의 특정 상품 즐겨찾기 조회
        Favorite favorite = favoriteRepository.findByUserIdAndProductId(userId, productId)
            .orElseThrow(() -> new RestApiException(_NOT_FOUND));
        
        // 2. 소프트 삭제
        favorite.deactivate();
        favoriteRepository.save(favorite);
    }
}
