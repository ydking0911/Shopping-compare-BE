package com.devmode.shop.global.annotation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.devmode.shop.domain.product.application.dto.request.ProductSearchRequest;
import com.devmode.shop.domain.product.application.dto.response.ProductResponse;
import com.devmode.shop.domain.product.application.dto.response.ProductSearchResponse;
import com.devmode.shop.global.common.BaseResponse;

/**
 * 상품 검색 관련 API 인터페이스
 */
@Tag(name = "상품 검색", description = "네이버 쇼핑 API를 활용한 상품 검색 및 가격비교")
public interface ProductApi extends BaseApi {
    
    @Operation(
        summary = "상품 검색 (POST)",
        description = "키워드로 상품을 검색하고 가격비교 정보를 제공합니다. 중고/렌탈/해외직구 제외, NPay 전용 등 필터링이 가능합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "상품 검색 성공",
            content = @Content(schema = @Schema(implementation = ProductSearchResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (유효성 검증 실패)",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "상품 검색 실패",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))
        )
    })
    BaseResponse<ProductSearchResponse> searchProducts(ProductSearchRequest request);
    
    @Operation(
        summary = "상품 검색 (GET)",
        description = "GET 방식으로 상품을 검색합니다. 간단한 검색에 적합합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "상품 검색 성공",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (유효성 검증 실패)",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "상품 검색 실패",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))
        )
    })
    BaseResponse<ProductResponse> searchProductsGet(String keyword, Integer page, Integer size, 
                                                  String sort, String excludeFilters, Boolean onlyNPay);
    
    @Operation(
        summary = "상품 검색 서비스 상태 확인",
        description = "상품 검색 서비스의 상태를 확인합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "서비스 정상",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))
        )
    })
    BaseResponse<String> healthCheck();
}
