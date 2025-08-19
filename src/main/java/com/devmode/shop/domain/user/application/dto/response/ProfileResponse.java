package com.devmode.shop.domain.user.application.dto.response;

import com.devmode.shop.domain.user.domain.entity.User;

public record ProfileResponse(
		Long id,
		String email,
		String name,
		String birth
) {
	public static ProfileResponse create(User user) {
		return new ProfileResponse(
				user.getId(),
				user.getEmail(),
				user.getName(),
				user.getBirth()
		);
	}
}


