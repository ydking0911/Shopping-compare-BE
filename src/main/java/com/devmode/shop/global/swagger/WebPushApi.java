package com.devmode.shop.global.swagger;

import com.devmode.shop.domain.notification.application.dto.request.RegisterWebPushSubscriptionRequest;
import com.devmode.shop.global.annotation.CurrentUser;
import com.devmode.shop.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Web Push 구독", description = "Web Push 구독 관리 API")
public interface WebPushApi extends BaseApi {
    
    @Operation(summary = "Web Push 구독 등록", description = "사용자의 Web Push 구독을 등록하거나 업데이트합니다.")
    @PostMapping
    BaseResponse<Void> registerWebPushSubscription(
            @Parameter(hidden = true) @CurrentUser String userId,
            @RequestBody RegisterWebPushSubscriptionRequest request);
    
    @Operation(summary = "Web Push 구독 삭제", description = "사용자의 Web Push 구독을 삭제합니다.")
    @DeleteMapping("/{endpoint}")
    BaseResponse<Void> deleteWebPushSubscription(
            @Parameter(hidden = true) @CurrentUser String userId,
            @PathVariable String endpoint);
}
