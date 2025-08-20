package com.devmode.shop.domain.user.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 토큰 재발급 요청 DTO
 */
public record TokenReissueRequest(
    @NotBlank(message = "refresh token은 필수입니다.")
    String refreshToken
) {}
