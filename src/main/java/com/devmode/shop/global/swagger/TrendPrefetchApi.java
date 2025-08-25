package com.devmode.shop.global.swagger;

import com.devmode.shop.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Trend Prefetch")
@RequestMapping("/api/trends/prefetch")
public interface TrendPrefetchApi {

    @Operation(summary = "DataLab 상위 키워드 시드")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PostMapping("/seed")
    BaseResponse<Void> seedFromDataLab();

    @Operation(summary = "트렌드 사전 캐싱 실행")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PostMapping
    BaseResponse<Void> prefetch();
}


