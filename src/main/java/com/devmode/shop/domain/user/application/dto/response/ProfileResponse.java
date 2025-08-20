package com.devmode.shop.domain.user.application.dto.response;

import com.devmode.shop.domain.user.domain.entity.User;

public record ProfileResponse(
		String userId,
		String email,
		String name,
		String birth
) {
	public static ProfileResponse create(User user) {
		return new ProfileResponse(
				user.getUserId(),
				user.getEmail(),
				user.getName(),
				user.getBirth()
		);
	}
}


