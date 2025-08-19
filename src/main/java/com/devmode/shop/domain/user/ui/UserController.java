package com.devmode.shop.domain.user.ui;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devmode.shop.domain.user.application.dto.response.ProfileResponse;
import com.devmode.shop.domain.user.application.usecase.UserProfileUseCase;
import com.devmode.shop.global.annotation.CurrentUser;
import com.devmode.shop.global.common.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

	private final UserProfileUseCase userProfileUseCase;

	@GetMapping("/profile")
	public BaseResponse<ProfileResponse> getProfile(
			@CurrentUser String userId) {
		return BaseResponse.onSuccess(userProfileUseCase.findProfile(userId));
	}
}
