package com.devmode.shop.domain.clickout.application.usecase;

import com.devmode.shop.domain.clickout.application.dto.response.ClickoutAnalyticsResponse;
import com.devmode.shop.domain.clickout.domain.repository.ProductClickLogRepository;
import com.devmode.shop.domain.clickout.domain.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
}
