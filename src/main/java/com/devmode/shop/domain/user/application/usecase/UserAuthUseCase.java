package com.devmode.shop.domain.user.application.usecase;

import java.time.Duration;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devmode.shop.domain.user.application.dto.request.LoginRequest;
import com.devmode.shop.domain.user.application.dto.request.SignUpRequest;
import com.devmode.shop.domain.user.application.dto.response.LoginResponse;
import com.devmode.shop.domain.user.domain.entity.User;
import com.devmode.shop.domain.user.domain.service.RefreshTokenService;
import com.devmode.shop.domain.user.domain.service.TokenBlacklistService;
import com.devmode.shop.domain.user.domain.service.TokenWhitelistService;
import com.devmode.shop.domain.user.domain.service.UserService;
import com.devmode.shop.global.exception.RestApiException;
import static com.devmode.shop.global.exception.code.status.AuthErrorStatus.ALREADY_REGISTERED_EMAIL;
import static com.devmode.shop.global.exception.code.status.AuthErrorStatus.ALREADY_REGISTERED_USER_ID;
import static com.devmode.shop.global.exception.code.status.AuthErrorStatus.EMPTY_JWT;
import static com.devmode.shop.global.exception.code.status.AuthErrorStatus.INVALID_ACCESS_TOKEN;
import static com.devmode.shop.global.exception.code.status.AuthErrorStatus.LOGIN_ERROR;
import com.devmode.shop.global.security.TokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserAuthUseCase {

	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final TokenProvider tokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final TokenWhitelistService tokenWhitelistService;
	private final TokenBlacklistService tokenBlacklistService;

	public void signUp(SignUpRequest request) {
		if (userService.isAlreadyRegistered(request.email())) {
			throw new RestApiException(ALREADY_REGISTERED_EMAIL);
		}
		if (userService.isUserIdAlreadyRegistered(request.userId())) {
			throw new RestApiException(ALREADY_REGISTERED_USER_ID);
		}
		String code = "USER_" + System.currentTimeMillis(); // 간단한 코드 생성
		userService.save(request, code);
	}

	public LoginResponse login(LoginRequest request) {
		User user = userService.findByUserId(request.userId());
		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new RestApiException(LOGIN_ERROR);
		}
		String access = tokenProvider.createAccessToken(user.getUserId());
		String refresh = tokenProvider.createRefreshToken(user.getUserId());
		Duration ttl = tokenProvider.getRemainingDuration(refresh).orElse(Duration.ofDays(14));
		refreshTokenService.saveRefreshToken(user.getUserId(), refresh, ttl);
		return new LoginResponse(access, refresh);
	}

	public void logout(HttpServletRequest request) {
		String accessToken = tokenProvider.getToken(request)
				.orElseThrow(() -> new RestApiException(EMPTY_JWT));
		String userId = tokenProvider.getId(accessToken)
				.orElseThrow(() -> new RestApiException(INVALID_ACCESS_TOKEN));
		Duration expiration = tokenProvider.getRemainingDuration(accessToken)
				.orElseThrow(() -> new RestApiException(INVALID_ACCESS_TOKEN));
		refreshTokenService.deleteRefreshToken(userId);
		tokenWhitelistService.deleteWhitelistToken(accessToken);
		tokenBlacklistService.blacklist(accessToken, expiration);
	}
}