package com.devmode.shop.domain.user.ui;

import com.devmode.shop.domain.user.application.dto.request.UpdateProfileRequest;
import com.devmode.shop.domain.user.application.usecase.UpdateProfileUseCase;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import com.devmode.shop.domain.user.application.dto.response.ProfileResponse;
import com.devmode.shop.domain.user.application.usecase.UserProfileUseCase;
import com.devmode.shop.global.annotation.CurrentUser;
import com.devmode.shop.global.annotation.UserProfileApi;
import com.devmode.shop.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController implements UserProfileApi {

	private final UserProfileUseCase userProfileUseCase; // 조회
	private final UpdateProfileUseCase updateProfileUseCase; // 수정

	@GetMapping("/profile")
	@Override
	public BaseResponse<ProfileResponse> getProfile(
			@Parameter(hidden = true) @CurrentUser String userId) {
		ProfileResponse profile = userProfileUseCase.findProfile(userId);
		return BaseResponse.onSuccess(profile);
	}

	@PatchMapping("/profile")
	@Override
	public BaseResponse<ProfileResponse> updateProfile(
			@Parameter(hidden = true) @CurrentUser String userId,
			@Valid @RequestBody UpdateProfileRequest request) {
		ProfileResponse updated = updateProfileUseCase.update(userId, request);
		return BaseResponse.onSuccess(updated);
	}
}