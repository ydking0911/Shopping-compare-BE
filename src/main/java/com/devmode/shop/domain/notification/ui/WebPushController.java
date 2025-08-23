package com.devmode.shop.domain.notification.ui;

import com.devmode.shop.domain.notification.application.dto.request.RegisterWebPushSubscriptionRequest;
import com.devmode.shop.domain.notification.domain.service.WebPushNotificationService;
import com.devmode.shop.global.annotation.CurrentUser;
import com.devmode.shop.global.common.BaseResponse;
import com.devmode.shop.global.swagger.WebPushApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/web-push-subscriptions")
public class WebPushController implements WebPushApi {
    
    private final WebPushNotificationService webPushNotificationService;
    
    @Override
    @PostMapping
    public BaseResponse<Void> registerWebPushSubscription(@CurrentUser String userId, @RequestBody @Valid RegisterWebPushSubscriptionRequest request) {
        webPushNotificationService.registerWebPushSubscription(
            userId,
            request.getEndpoint(),
            request.getP256dhKey(),
            request.getAuthSecret(),
            request.getBrowserInfo()
        );
        
        return BaseResponse.onSuccess();
    }
    
    @Override
    @DeleteMapping("/{endpoint}")
    public BaseResponse<Void> deleteWebPushSubscription(@CurrentUser String userId, @PathVariable String endpoint) {
        webPushNotificationService.deleteWebPushSubscription(userId, endpoint);
        return BaseResponse.onSuccess();
    }
}
