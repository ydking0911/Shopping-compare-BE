package com.devmode.shop.domain.favorite.application.dto.response;

import com.devmode.shop.domain.favorite.domain.entity.Favorite;
import com.devmode.shop.domain.product.application.dto.response.ProductItem;

import java.time.LocalDateTime;

public record FavoriteResponse(
    Long id,
    String userId,
    ProductItem product,
    String memo,
    String favoriteGroup,
    Boolean notificationEnabled,
    Integer targetPrice,
    Integer priority,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    // ProductMapper를 사용하여 변환하는 메서드
    public static FavoriteResponse from(Favorite favorite, ProductItem productItem) {
        return new FavoriteResponse(
            favorite.getId(),
            favorite.getUserId(),
            productItem,
            favorite.getMemo(),
            favorite.getFavoriteGroup(),
            favorite.getNotificationEnabled(),
            favorite.getTargetPrice(),
            favorite.getPriority(),
            favorite.getIsActive(),
            favorite.getCreatedAt(),
            favorite.getUpdatedAt()
        );
    }
}
