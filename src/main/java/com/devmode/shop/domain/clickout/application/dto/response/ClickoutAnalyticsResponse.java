package com.devmode.shop.domain.clickout.application.dto.response;

import java.time.LocalDate;
import java.util.List;

public record ClickoutAnalyticsResponse(
        String keyword,
        LocalDate startDate,
        LocalDate endDate,
        List<ClickoutStatistic> clickStatistics,
        List<PriceTrend> priceTrends,
        List<PopularProduct> popularProducts
) {
    
    public record ClickoutStatistic(
            String category,
            Long clickCount,
            Long uniqueUsers,
            Double averagePrice
    ) {}
    
    public record PriceTrend(
            String productId,
            String productTitle,
            String priceChange,
            Double priceChangePercentage,
            LocalDate recordedDate
    ) {}
    
    public record PopularProduct(
            String productId,
            String productTitle,
            Long clickCount,
            String category,
            String brand
    ) {}
}
