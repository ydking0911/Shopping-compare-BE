package com.devmode.shop.domain.favorite.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateFavoriteRequest(
    @NotNull(message = "사용자 ID는 필수입니다")
    String userId,
    
    @NotNull(message = "상품 ID는 필수입니다")
    Long productId,
    
    String memo,
    String favoriteGroup,
    Boolean notificationEnabled,
    Integer targetPrice,
    Integer priority
) {}
