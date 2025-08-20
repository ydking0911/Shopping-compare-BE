package com.devmode.shop.domain.user.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.devmode.shop.domain.user.application.dto.request.LoginRequest;
import com.devmode.shop.domain.user.application.dto.request.SignUpRequest;
import com.devmode.shop.domain.user.application.dto.request.TokenReissueRequest;
import com.devmode.shop.domain.user.application.dto.response.LoginResponse;
import com.devmode.shop.domain.user.application.dto.response.TokenReissueResponse;
import com.devmode.shop.domain.user.domain.entity.User;
import com.devmode.shop.domain.user.domain.service.RefreshTokenService;
import com.devmode.shop.domain.user.domain.service.TokenBlacklistService;
import com.devmode.shop.domain.user.domain.service.TokenWhitelistService;
import com.devmode.shop.domain.user.domain.service.UserService;
import com.devmode.shop.global.exception.RestApiException;
import com.devmode.shop.global.security.TokenProvider;

@ExtendWith(MockitoExtension.class)
class UserAuthUseCaseTest {

	@Mock private UserService userService;
	@Mock private PasswordEncoder passwordEncoder;
	@Mock private TokenProvider tokenProvider;
	@Mock private RefreshTokenService refreshTokenService;
	@Mock private TokenWhitelistService tokenWhitelistService;
	@Mock private TokenBlacklistService tokenBlacklistService;

	@InjectMocks
	private UserAuthUseCase useCase;

	private SignUpRequest signUpRequest;

	@BeforeEach
	void setUp() {
		signUpRequest = new SignUpRequest("testuser", "test@example.com", "password", "Tester", "1990-01-01");
	}

	@Test
	@DisplayName("회원가입 성공: 이메일/아이디 중복 아님")
	void signUp_success() {
		when(userService.isAlreadyRegistered(signUpRequest.email())).thenReturn(false);
		when(userService.isUserIdAlreadyRegistered(signUpRequest.userId())).thenReturn(false);

		assertDoesNotThrow(() -> useCase.signUp(signUpRequest));
		verify(userService).save(signUpRequest);
	}

	@Test
	@DisplayName("회원가입 실패: 이메일 중복")
	void signUp_duplicateEmail_throws() {
		when(userService.isAlreadyRegistered(signUpRequest.email())).thenReturn(true);

		assertThrows(RestApiException.class, () -> useCase.signUp(signUpRequest));
		verify(userService, never()).save(any());
	}

	@Test
	@DisplayName("로그인 성공")
	void login_success() {
		LoginRequest req = new LoginRequest("testuser", "password");
		User user = User.builder()
				.userId("testuser")
				.email("test@example.com")
				.password("encoded")
				.name("Tester")
				.birth("1990-01-01")
				.build();

		when(userService.findByUserId("testuser")).thenReturn(user);
		when(passwordEncoder.matches("password", "encoded")).thenReturn(true);
		when(tokenProvider.createAccessToken("testuser")).thenReturn("access");
		when(tokenProvider.createRefreshToken("testuser")).thenReturn("refresh");
		when(tokenProvider.getRemainingDuration("refresh")).thenReturn(Optional.of(Duration.ofDays(14)));

		LoginResponse res = useCase.login(req);
		assertNotNull(res);
		assertEquals("access", res.accessToken());
		assertEquals("refresh", res.refreshToken());
		verify(refreshTokenService).saveRefreshToken(eq("testuser"), eq("refresh"), any());
	}

	@Test
	@DisplayName("로그인 실패: 비밀번호 불일치")
	void login_wrongPassword_throws() {
		LoginRequest req = new LoginRequest("testuser", "wrong");
		User user = User.builder()
				.userId("testuser")
				.email("test@example.com")
				.password("encoded")
				.name("Tester")
				.birth("1990-01-01")
				.build();
		when(userService.findByUserId("testuser")).thenReturn(user);
		when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

		assertThrows(RestApiException.class, () -> useCase.login(req));
	}

	@Test
	@DisplayName("리프레시 토큰 재발급 성공")
	void reissue_success() {
		TokenReissueRequest req = new TokenReissueRequest("refresh-old");
		when(tokenProvider.validateToken("refresh-old")).thenReturn(true);
		when(tokenProvider.getId("refresh-old")).thenReturn(Optional.of("testuser"));
		when(refreshTokenService.findByUserId("testuser")).thenReturn("refresh-old");
		when(tokenProvider.getRemainingDuration("refresh-old")).thenReturn(Optional.of(Duration.ofMinutes(10)));
		when(tokenProvider.createAccessToken("testuser")).thenReturn("access-new");
		when(tokenProvider.createRefreshToken("testuser")).thenReturn("refresh-new");
		when(tokenProvider.getRemainingDuration("refresh-new")).thenReturn(Optional.of(Duration.ofDays(14)));

		TokenReissueResponse res = useCase.reissueToken(req);
		assertEquals("access-new", res.accessToken());
		assertEquals("refresh-new", res.refreshToken());
		verify(refreshTokenService).deleteRefreshToken("testuser");
		verify(refreshTokenService).saveRefreshToken(eq("testuser"), eq("refresh-new"), any());
		verify(tokenBlacklistService).blacklist(eq("refresh-old"), any());
	}
}
