package com.devmode.shop.global.swagger;

import com.devmode.shop.domain.clickout.application.dto.request.ProductClickRequest;
import com.devmode.shop.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 클릭아웃 API를 위한 기본 인터페이스
 */
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "클릭아웃 API", description = "상품 클릭 로깅 및 분석 API")
public interface ClickoutApi {
    
    /**
     * 상품 클릭 로깅
     */
    BaseResponse<Void> logProductClick(ProductClickRequest request, HttpServletRequest httpRequest);
    
    /**
     * 공통 응답 스키마 정의
     */
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "요청 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "리소스를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))
        )
    })
    default void commonResponses() {}
}
