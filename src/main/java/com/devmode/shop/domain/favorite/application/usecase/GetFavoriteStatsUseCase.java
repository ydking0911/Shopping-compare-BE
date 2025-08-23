package com.devmode.shop.domain.favorite.application.usecase;

import com.devmode.shop.domain.favorite.application.dto.response.FavoriteStatsResponse;
import com.devmode.shop.domain.favorite.domain.repository.FavoriteRepository;
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
public class GetFavoriteStatsUseCase {
    
    private final FavoriteRepository favoriteRepository;
    
    public FavoriteStatsResponse getFavoriteStats(String userId) {
        log.info("사용자 {}의 즐겨찾기 통계 조회 시작", userId);
        
        try {
            // 1. 기본 통계 조회
            long totalFavorites = favoriteRepository.countByUserIdAndIsActiveTrue(userId);
            
            if (totalFavorites == 0) {
                log.info("사용자 {}의 즐겨찾기가 없습니다.", userId);
                return new FavoriteStatsResponse(
                    0L, 0L, List.of(), List.of(), List.of()
                );
            }
            
            // 2. 카테고리별 통계 (실제 DB 쿼리 사용)
            List<FavoriteStatsResponse.CategoryStats> categoryStats = getCategoryStats(userId);
            
            // 3. 브랜드별 통계 (실제 DB 쿼리 사용)
            List<FavoriteStatsResponse.BrandStats> brandStats = getBrandStats(userId);
            
            // 4. 쇼핑몰별 통계 (실제 DB 쿼리 사용)
            List<FavoriteStatsResponse.MallStats> mallStats = getMallStats(userId);
            
            log.info("사용자 {}의 즐겨찾기 통계 조회 완료: 총 {}개", userId, totalFavorites);
            
            return new FavoriteStatsResponse(
                totalFavorites,
                totalFavorites, // totalProducts는 totalFavorites와 동일
                categoryStats,
                brandStats,
                mallStats
            );
            
        } catch (Exception e) {
            log.error("사용자 {}의 즐겨찾기 통계 조회 중 오류 발생: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
    
    private List<FavoriteStatsResponse.CategoryStats> getCategoryStats(String userId) {
        try {
            List<Object[]> results = favoriteRepository.getCategoryStats(userId);
            
            return results.stream()
                .map(result -> new FavoriteStatsResponse.CategoryStats(
                    (String) result[0],           // category
                    ((Number) result[1]).longValue(), // count
                    ((Number) result[2]).doubleValue() // percentage
                ))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("카테고리별 통계 조회 중 오류 발생: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    private List<FavoriteStatsResponse.BrandStats> getBrandStats(String userId) {
        try {
            List<Object[]> results = favoriteRepository.getBrandStats(userId);
            
            return results.stream()
                .map(result -> new FavoriteStatsResponse.BrandStats(
                    (String) result[0],           // brand
                    ((Number) result[1]).longValue(), // count
                    ((Number) result[2]).doubleValue() // percentage
                ))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("브랜드별 통계 조회 중 오류 발생: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    private List<FavoriteStatsResponse.MallStats> getMallStats(String userId) {
        try {
            List<Object[]> results = favoriteRepository.getMallStats(userId);
            
            return results.stream()
                .map(result -> new FavoriteStatsResponse.MallStats(
                    (String) result[0],           // mallName
                    ((Number) result[1]).longValue(), // count
                    ((Number) result[2]).doubleValue() // percentage
                ))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("쇼핑몰별 통계 조회 중 오류 발생: {}", e.getMessage(), e);
            return List.of();
        }
    }
}
