package com.devmode.shop.domain.user.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devmode.shop.domain.user.application.dto.response.ProfileResponse;
import com.devmode.shop.domain.user.domain.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserProfileUseCase {

	private final UserService userService;

	public ProfileResponse findProfile(String userId) {
		ProfileResponse profileDto = userService.findProfile(userId);

		return new ProfileResponse(
				profileDto.userId(),
				profileDto.email(),
				profileDto.name(),
				profileDto.birth()
		);
	}
}


