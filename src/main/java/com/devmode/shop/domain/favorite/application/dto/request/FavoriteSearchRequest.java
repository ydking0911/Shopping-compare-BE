package com.devmode.shop.domain.favorite.application.dto.request;

public record FavoriteSearchRequest(
    String category,
    String brand,
    String mallName,
    Integer minPrice,
    Integer maxPrice,
    String sortBy,
    Integer page,
    Integer size
) {
    public FavoriteSearchRequest {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1 || size > 100) size = 20;
    }
}
