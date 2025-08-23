package com.devmode.shop.domain.notification.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterWebPushSubscriptionRequest {
    
    @NotBlank(message = "endpoint는 필수입니다.")
    private String endpoint;
    
    @NotBlank(message = "p256dhKey는 필수입니다.")
    private String p256dhKey;
    
    @NotBlank(message = "authSecret은 필수입니다.")
    private String authSecret;
    
    private String browserInfo;
}
