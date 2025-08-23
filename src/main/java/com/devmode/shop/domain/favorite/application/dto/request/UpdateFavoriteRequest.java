package com.devmode.shop.domain.favorite.application.dto.request;

public record UpdateFavoriteRequest(
    String memo,
    String favoriteGroup,
    Boolean notificationEnabled,
    Integer targetPrice,
    Integer priority
) {}
