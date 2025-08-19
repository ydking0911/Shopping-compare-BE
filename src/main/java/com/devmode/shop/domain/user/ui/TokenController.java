package com.devmode.shop.domain.user.ui;

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
public class TokenController {

	private final TokenReissueService tokenReissueService;

	@PostMapping
	public BaseResponse<TokenReissueResponse> reissue(
			@RefreshToken String refreshToken,
			@CurrentUser String userId) {
		return BaseResponse.onSuccess(tokenReissueService.reissue(refreshToken, userId));
	}
}