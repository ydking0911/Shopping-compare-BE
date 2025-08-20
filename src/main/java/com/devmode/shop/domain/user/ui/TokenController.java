package com.devmode.shop.domain.user.ui;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devmode.shop.domain.user.application.dto.response.TokenReissueResponse;
import com.devmode.shop.domain.user.domain.service.TokenReissueService;
import com.devmode.shop.global.annotation.CurrentUser;
import com.devmode.shop.global.annotation.RefreshToken;
import com.devmode.shop.global.common.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
@Tag(name = "토큰", description = "JWT 토큰 관련 API")
public class TokenController {

	private final TokenReissueService tokenReissueService;

	@PostMapping
	@Operation(summary = "토큰 재발급", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급받습니다.")
	public BaseResponse<TokenReissueResponse> reissue(
			@RefreshToken String refreshToken,
			@CurrentUser String userId) {
		return BaseResponse.onSuccess(tokenReissueService.reissue(refreshToken, userId));
	}
}