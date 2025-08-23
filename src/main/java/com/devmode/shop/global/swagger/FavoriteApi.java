package com.devmode.shop.global.swagger;

import com.devmode.shop.domain.favorite.application.dto.request.CreateFavoriteRequest;
import com.devmode.shop.domain.favorite.application.dto.request.UpdateFavoriteRequest;
import com.devmode.shop.domain.favorite.application.dto.request.FavoriteSearchRequest;
import com.devmode.shop.domain.favorite.application.dto.response.FavoriteResponse;
import com.devmode.shop.domain.favorite.application.dto.response.FavoriteListResponse;
import com.devmode.shop.domain.favorite.application.dto.response.FavoriteStatsResponse;
import com.devmode.shop.global.annotation.CurrentUser;
import com.devmode.shop.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "즐겨찾기", description = "즐겨찾기 관련 API")
public interface FavoriteApi extends BaseApi {
    
    @Operation(summary = "즐겨찾기 생성", description = "상품을 즐겨찾기에 추가합니다.")
    @PostMapping
    BaseResponse<FavoriteResponse> createFavorite(
            @Parameter(hidden = true) @CurrentUser String userId,
            @RequestBody CreateFavoriteRequest request);
    
    @Operation(summary = "즐겨찾기 목록 조회", description = "사용자의 즐겨찾기 목록을 조회합니다.")
    @GetMapping
    BaseResponse<FavoriteListResponse> getFavoriteList(
            @Parameter(hidden = true) @CurrentUser String userId,
            @ModelAttribute FavoriteSearchRequest request);
    
    @Operation(summary = "즐겨찾기 통계 조회", description = "사용자의 즐겨찾기 통계를 조회합니다.")
    @GetMapping("/stats")
    BaseResponse<FavoriteStatsResponse> getFavoriteStats(
            @Parameter(hidden = true) @CurrentUser String userId);
    
    @Operation(summary = "즐겨찾기 수정", description = "즐겨찾기 정보를 수정합니다.")
    @PutMapping("/{favoriteId}")
    BaseResponse<FavoriteResponse> updateFavorite(
            @Parameter(hidden = true) @CurrentUser String userId,
            @PathVariable Long favoriteId, 
            @RequestBody UpdateFavoriteRequest request);
    
    @Operation(summary = "즐겨찾기 삭제", description = "즐겨찾기를 삭제합니다.")
    @DeleteMapping("/{favoriteId}")
    BaseResponse<Void> deleteFavorite(
            @Parameter(hidden = true) @CurrentUser String userId,
            @PathVariable Long favoriteId);
    
    @Operation(summary = "상품별 즐겨찾기 삭제", description = "특정 상품의 즐겨찾기를 삭제합니다.")
    @DeleteMapping("/products/{productId}")
    BaseResponse<Void> deleteFavoriteByProductId(
            @Parameter(hidden = true) @CurrentUser String userId,
            @PathVariable Long productId);
}
