package com.devmode.shop.domain.user.domain.service;

import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devmode.shop.domain.user.application.dto.response.TokenReissueResponse;
import com.devmode.shop.domain.user.domain.entity.User;
import com.devmode.shop.global.exception.RestApiException;
import static com.devmode.shop.global.exception.code.status.AuthErrorStatus.EXPIRED_MEMBER_JWT;
import static com.devmode.shop.global.exception.code.status.AuthErrorStatus.INVALID_REFRESH_TOKEN;
import com.devmode.shop.global.security.TokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenReissueService {

	private final TokenProvider tokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final UserService userService;

	public TokenReissueResponse reissue(String refreshToken, String userId) {

		// 존재 유무 검사
		if (!refreshTokenService.isExist(refreshToken, userId)) {
			throw new RestApiException(INVALID_REFRESH_TOKEN);
		}

		// 기존에 있는 토큰 삭제
		refreshTokenService.deleteRefreshToken(userId);

		// 새 토큰 발급
		User user = userService.findUser(userId);
		String newAccessToken = tokenProvider.createAccessToken(userId);
		String newRefreshToken = tokenProvider.createRefreshToken(userId);
		Duration duration = tokenProvider.getRemainingDuration(refreshToken)
				.orElseThrow(() -> new RestApiException(EXPIRED_MEMBER_JWT));

		// 저장
		refreshTokenService.saveRefreshToken(userId, newRefreshToken, duration);

		return new TokenReissueResponse(newAccessToken, newRefreshToken);
	}
}