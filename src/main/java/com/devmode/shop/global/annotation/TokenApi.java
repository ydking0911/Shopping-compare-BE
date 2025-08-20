package com.devmode.shop.global.annotation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.devmode.shop.domain.user.application.dto.response.TokenReissueResponse;
import com.devmode.shop.global.common.BaseResponse;

/**
 * JWT 토큰 관련 API 인터페이스 (TokenController용)
 */
@Tag(name = "토큰 관리", description = "JWT 토큰 재발급 및 관리 API")
public interface TokenApi extends BaseApi {

    @Operation(
        summary = "토큰 재발급",
        description = "refresh token을 사용하여 새로운 access token을 발급받습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "토큰 재발급 성공",
            content = @Content(schema = @Schema(implementation = TokenReissueResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 refresh token",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "만료된 refresh token",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))
        )
    })
    BaseResponse<TokenReissueResponse> reissue(String refreshToken, String userId);
}