package com.devmode.shop.domain.user.application.usecase;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devmode.shop.domain.user.application.dto.request.UpdateProfileRequest;
import com.devmode.shop.domain.user.application.dto.response.ProfileResponse;
import com.devmode.shop.domain.user.domain.entity.User;
import com.devmode.shop.domain.user.domain.service.UserService;
import com.devmode.shop.global.exception.RestApiException;
import static com.devmode.shop.global.exception.code.status.GlobalErrorStatus._NOT_FOUND;
import static com.devmode.shop.global.exception.code.status.GlobalErrorStatus._UNAUTHORIZED;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateProfileUseCase {

	private final UserService userService;
	private final PasswordEncoder passwordEncoder;

	public ProfileResponse update(String userId, UpdateProfileRequest request) {
		User user = userService.findUser(userId);

		String encodedNewPassword = null;

		// 새 비번 요청이 있는 경우 → 현재 비번 일치 여부 검사 (불일치 시 권한 오류)
		if (request.newPassword() != null && !request.newPassword().isBlank()) {
			boolean matches = passwordEncoder.matches(request.currentPassword(), user.getPassword());
			if (!matches) {
				throw new RestApiException(_UNAUTHORIZED);
			}
			encodedNewPassword = passwordEncoder.encode(request.newPassword());
		}

		user.updateProfile(request.name(), request.birth(), encodedNewPassword);
		return ProfileResponse.create(user);
	}
}


