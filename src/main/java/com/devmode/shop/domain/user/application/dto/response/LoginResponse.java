package com.devmode.shop.domain.user.application.dto.response;

public record LoginResponse(
		String accessToken,
		String refreshToken
) {}


