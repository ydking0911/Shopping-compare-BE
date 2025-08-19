package com.devmode.shop.domain.user.application.dto.response;

public record TokenReissueResponse(
		String accessToken,
		String refreshToken
) {}


