package com.devmode.shop.domain.favorite.application.dto.response;

import java.util.List;

public record FavoriteStatsResponse(
    long totalFavorites,
    long totalProducts,
    List<CategoryStats> categoryStats,
    List<BrandStats> brandStats,
    List<MallStats> mallStats
) {
    
    public record CategoryStats(
        String category,
        long count,
        double percentage
    ) {}
    
    public record BrandStats(
        String brand,
        long count,
        double percentage
    ) {}
    
    public record MallStats(
        String mallName,
        long count,
        double percentage
    ) {}
}
