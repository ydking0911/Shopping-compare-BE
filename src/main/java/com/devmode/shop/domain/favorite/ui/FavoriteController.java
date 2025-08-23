package com.devmode.shop.domain.favorite.ui;

import com.devmode.shop.domain.favorite.application.dto.request.CreateFavoriteRequest;
import com.devmode.shop.domain.favorite.application.dto.request.UpdateFavoriteRequest;
import com.devmode.shop.domain.favorite.application.dto.request.FavoriteSearchRequest;
import com.devmode.shop.domain.favorite.application.dto.response.FavoriteResponse;
import com.devmode.shop.domain.favorite.application.dto.response.FavoriteListResponse;
import com.devmode.shop.domain.favorite.application.dto.response.FavoriteStatsResponse;
import com.devmode.shop.domain.favorite.application.usecase.*;
import com.devmode.shop.global.annotation.CurrentUser;
import com.devmode.shop.global.common.BaseResponse;
import com.devmode.shop.global.swagger.FavoriteApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/favorites")
public class FavoriteController implements FavoriteApi {
    
    private final CreateFavoriteUseCase createFavoriteUseCase;
    private final UpdateFavoriteUseCase updateFavoriteUseCase;
    private final DeleteFavoriteUseCase deleteFavoriteUseCase;
    private final GetFavoriteListUseCase getFavoriteListUseCase;
    private final GetFavoriteStatsUseCase getFavoriteStatsUseCase;
    
    @Override
    @PostMapping
    public BaseResponse<FavoriteResponse> createFavorite(@CurrentUser String userId, @RequestBody CreateFavoriteRequest request) {
        // userId를 request에 설정
        CreateFavoriteRequest createRequest = new CreateFavoriteRequest(
            userId,
            request.productId(),
            request.memo(),
            request.favoriteGroup(),
            request.notificationEnabled(),
            request.targetPrice(),
            request.priority()
        );
        FavoriteResponse response = createFavoriteUseCase.createFavorite(createRequest);
        return BaseResponse.onSuccess(response);
    }
    
    @Override
    @GetMapping
    public BaseResponse<FavoriteListResponse> getFavoriteList(@CurrentUser String userId, @ModelAttribute FavoriteSearchRequest request) {
        FavoriteListResponse response = getFavoriteListUseCase.getFavoriteList(userId, request);
        return BaseResponse.onSuccess(response);
    }
    
    @Override
    @GetMapping("/stats")
    public BaseResponse<FavoriteStatsResponse> getFavoriteStats(@CurrentUser String userId) {
        FavoriteStatsResponse response = getFavoriteStatsUseCase.getFavoriteStats(userId);
        return BaseResponse.onSuccess(response);
    }
    
    @Override
    @PutMapping("/{favoriteId}")
    public BaseResponse<FavoriteResponse> updateFavorite(@CurrentUser String userId, @PathVariable Long favoriteId, @RequestBody UpdateFavoriteRequest request) {
        FavoriteResponse response = updateFavoriteUseCase.updateFavorite(userId, favoriteId, request);
        return BaseResponse.onSuccess(response);
    }
    
    @Override
    @DeleteMapping("/{favoriteId}")
    public BaseResponse<Void> deleteFavorite(@CurrentUser String userId, @PathVariable Long favoriteId) {
        deleteFavoriteUseCase.deleteFavorite(userId, favoriteId);
        return BaseResponse.onSuccess();
    }
    
    @Override
    @DeleteMapping("/products/{productId}")
    public BaseResponse<Void> deleteFavoriteByProductId(@CurrentUser String userId, @PathVariable Long productId) {
        deleteFavoriteUseCase.deleteFavoriteByProductId(userId, productId);
        return BaseResponse.onSuccess();
    }
}
