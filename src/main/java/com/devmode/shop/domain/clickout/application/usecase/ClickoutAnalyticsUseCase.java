package com.devmode.shop.domain.clickout.application.usecase;

import com.devmode.shop.domain.clickout.application.dto.response.ClickoutAnalyticsResponse;
import com.devmode.shop.domain.clickout.domain.repository.ProductClickLogRepository;
import com.devmode.shop.domain.clickout.domain.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClickoutAnalyticsUseCase {
    
    private final ProductClickLogRepository productClickLogRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    
    /**
     * 키워드별 클릭아웃 통계 조회
     */
    public ClickoutAnalyticsResponse getClickoutStatistics(String keyword, LocalDate startDate, LocalDate endDate) {
        // 클릭아웃 통계 조회
        List<Object[]> clickoutStats = productClickLogRepository.findClickoutStatisticsByKeywordAndDateRange(keyword, startDate, endDate);
        List<ClickoutAnalyticsResponse.ClickoutStatistic> clickStatistics = clickoutStats.stream()
                .map(this::mapToClickoutStatistic)
                .collect(Collectors.toList());
        
        // 인기 상품 조회
        List<Object[]> popularProducts = productClickLogRepository.findPopularProductsByKeywordAndDateRange(keyword, startDate, endDate);
        List<ClickoutAnalyticsResponse.PopularProduct> productList = popularProducts.stream()
                .map(this::mapToPopularProduct)
                .collect(Collectors.toList());
        
        // 가격 트렌드 조회
        List<Object[]> priceTrends = priceHistoryRepository.findPriceTrendsByKeywordAndDateRange(keyword, startDate, endDate);
        List<ClickoutAnalyticsResponse.PriceTrend> trendList = priceTrends.stream()
                .map(this::mapToPriceTrend)
                .collect(Collectors.toList());
        
        return new ClickoutAnalyticsResponse(
                keyword,
                startDate,
                endDate,
                clickStatistics,
                trendList,
                productList
        );
    }
    
    /**
     * 인기 키워드 조회
     */
    public List<String> getPopularKeywords(LocalDate date) {
        List<Object[]> keywords = productClickLogRepository.findPopularKeywordsByDate(date);
        return keywords.stream()
                .map(row -> (String) row[0])
                .collect(Collectors.toList());
    }
    
    /**
     * 카테고리별 인기도 조회
     */
    public List<ClickoutAnalyticsResponse.ClickoutStatistic> getCategoryPopularity(LocalDate date) {
        List<Object[]> categoryStats = productClickLogRepository.findCategoryPopularityByDate(date);
        return categoryStats.stream()
                .map(this::mapToClickoutStatistic)
                .collect(Collectors.toList());
    }
    
    /**
     * Object[]를 ClickoutStatistic으로 변환
     */
    private ClickoutAnalyticsResponse.ClickoutStatistic mapToClickoutStatistic(Object[] row) {
        String category = (String) row[0];
        Long clickCount = (Long) row[1];
        
        // uniqueUsers와 averagePrice는 카테고리별 인기도 조회에서는 사용하지 않음
        Long uniqueUsers = row.length > 2 ? (Long) row[2] : clickCount;
        Double averagePrice = row.length > 3 ? (Double) row[3] : 0.0;
        
        return new ClickoutAnalyticsResponse.ClickoutStatistic(
                category != null ? category : "기타",
                clickCount != null ? clickCount : 0L,
                uniqueUsers != null ? uniqueUsers : 0L,
                averagePrice != null ? averagePrice : 0.0
        );
    }
    
    /**
     * Object[]를 PopularProduct로 변환
     */
    private ClickoutAnalyticsResponse.PopularProduct mapToPopularProduct(Object[] row) {
        return new ClickoutAnalyticsResponse.PopularProduct(
                (String) row[0], // productId
                (String) row[1], // productTitle
                (Long) row[2],   // clickCount
                (String) row[3], // category
                (String) row[4]  // brand
        );
    }

    /**
     * Object[]를 PriceTrend로 변환
     */
    private ClickoutAnalyticsResponse.PriceTrend mapToPriceTrend(Object[] row) {
        return new ClickoutAnalyticsResponse.PriceTrend(
                (String) row[0], // productId
                (String) row[1], // productTitle
                (String) row[2], // trend
                (Double) row[3], // percentageChange
                (LocalDate) row[4] // date
        );
    }
    
    /**
     * 사용자 클릭 히스토리 조회
     */
    public List<String> getUserClickHistory(String userId, LocalDate startDate, LocalDate endDate) {
        log.info("사용자 클릭 히스토리 조회: userId={}, startDate={}, endDate={}", userId, startDate, endDate);
        
        try {
            // 사용자의 클릭 로그에서 상품 ID 목록 조회
            List<String> productIds = productClickLogRepository.findProductIdsByUserIdAndDateRange(userId, startDate, endDate);
            
            log.info("사용자 클릭 히스토리 조회 완료: userId={}, 상품 수={}", userId, productIds.size());
            return productIds;
            
        } catch (Exception e) {
            log.error("사용자 클릭 히스토리 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * 개인화된 추천 상품 조회
     */
    public List<String> getPersonalizedRecommendations(String userId) {
        log.info("개인화된 추천 상품 조회: userId={}", userId);
        
        try {
            // 1. 사용자의 클릭 패턴 분석
            List<String> userCategories = productClickLogRepository.findUserPreferredCategories(userId);
            List<String> userBrands = productClickLogRepository.findUserPreferredBrands(userId);
            
            // 2. 사용자 선호도 기반 추천 상품 조회
            List<String> recommendedProducts = productClickLogRepository.findRecommendedProductsByPreferences(
                userCategories, userBrands, userId);
            
            // 3. 인기 상품과 결합하여 최종 추천 목록 생성
            List<String> popularProducts = productClickLogRepository.findPopularProductsExcludingUser(userId, 10);
            
            // 사용자 선호도 기반 추천을 우선으로 하고, 인기 상품으로 보완
            List<String> finalRecommendations = new ArrayList<>();
            finalRecommendations.addAll(recommendedProducts);
            
            // 중복 제거하면서 인기 상품 추가
            for (String productId : popularProducts) {
                if (!finalRecommendations.contains(productId) && finalRecommendations.size() < 20) {
                    finalRecommendations.add(productId);
                }
            }
            
            log.info("개인화된 추천 상품 조회 완료: userId={}, 추천 상품 수={}", userId, finalRecommendations.size());
            return finalRecommendations;
            
        } catch (Exception e) {
            log.error("개인화된 추천 상품 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            // 실패 시 인기 상품 반환
            return productClickLogRepository.findPopularProducts(10);
        }
    }
    
    /**
     * 사용자 클릭 통계 조회
     */
    public ClickoutAnalyticsResponse getUserClickStatistics(String userId, LocalDate startDate, LocalDate endDate) {
        log.info("사용자 클릭 통계 조회: userId={}, startDate={}, endDate={}", userId, startDate, endDate);
        
        try {
            // 1. 사용자별 클릭 통계 조회
            List<Object[]> userClickStats = productClickLogRepository.findUserClickStatistics(userId, startDate, endDate);
            List<ClickoutAnalyticsResponse.ClickoutStatistic> clickStatistics = userClickStats.stream()
                    .map(this::mapToClickoutStatistic)
                    .collect(Collectors.toList());
            
            // 2. 사용자별 인기 상품 조회
            List<Object[]> userPopularProducts = productClickLogRepository.findUserPopularProducts(userId, startDate, endDate);
            List<ClickoutAnalyticsResponse.PopularProduct> productList = userPopularProducts.stream()
                    .map(this::mapToPopularProduct)
                    .collect(Collectors.toList());
            
            // 3. 사용자별 가격 트렌드 조회
            List<Object[]> userPriceTrends = priceHistoryRepository.findUserPriceTrends(userId, startDate, endDate);
            List<ClickoutAnalyticsResponse.PriceTrend> trendList = userPriceTrends.stream()
                    .map(this::mapToPriceTrend)
                    .collect(Collectors.toList());
            
            ClickoutAnalyticsResponse response = new ClickoutAnalyticsResponse(
                    "user_" + userId,
                    startDate,
                    endDate,
                    clickStatistics,
                    trendList,
                    productList
            );
            
            log.info("사용자 클릭 통계 조회 완료: userId={}, 통계 수={}, 상품 수={}, 트렌드 수={}", 
                    userId, clickStatistics.size(), productList.size(), trendList.size());
            
            return response;
            
        } catch (Exception e) {
            log.error("사용자 클릭 통계 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            
            // 실패 시 기본 응답 반환
            return new ClickoutAnalyticsResponse(
                    "user_" + userId,
                    startDate,
                    endDate,
                    List.of(),
                    List.of(),
                    List.of()
            );
        }
    }
}
