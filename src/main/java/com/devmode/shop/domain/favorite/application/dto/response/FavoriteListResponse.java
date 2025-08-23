package com.devmode.shop.domain.favorite.application.dto.response;

import java.util.List;

public record FavoriteListResponse(
    List<FavoriteResponse> favorites,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public static FavoriteListResponse from(List<FavoriteResponse> favorites, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new FavoriteListResponse(favorites, page, size, totalElements, totalPages);
    }
}
