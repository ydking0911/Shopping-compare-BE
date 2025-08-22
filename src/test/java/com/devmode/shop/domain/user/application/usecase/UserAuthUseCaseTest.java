package com.devmode.shop.domain.user.application.usecase;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAuthUseCaseTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private TokenWhitelistService tokenWhitelistService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private UserAuthUseCase userAuthUseCase;

    private User mockUser;
    private SignUpRequest signUpRequest;
    private LoginRequest loginRequest;
    private TokenReissueRequest tokenReissueRequest;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .userId("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .name("테스트유저")
                .birth("1990-01-01")
                .build();

        signUpRequest = new SignUpRequest(
                "test@example.com",  // email (첫 번째)
                "testuser",          // userId (두 번째)
                "password123",
                "테스트유저",
                "1990-01-01"
        );

        loginRequest = new LoginRequest(
                "testuser",
                "password123"
        );

        tokenReissueRequest = new TokenReissueRequest("refreshToken123");
    }

    @Test
    @DisplayName("회원가입을 성공적으로 처리할 수 있다")
    void signUpSuccess() {
        // given
        when(userService.isAlreadyRegistered(anyString())).thenReturn(false);
        when(userService.isUserIdAlreadyRegistered(anyString())).thenReturn(false);
        when(userService.save(any(SignUpRequest.class))).thenReturn(mockUser);

        // when
        userAuthUseCase.signUp(signUpRequest);

        // then
        verify(userService).isAlreadyRegistered("test@example.com");
        verify(userService).isUserIdAlreadyRegistered("testuser");
        verify(userService).save(signUpRequest);
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입 시 예외를 던진다")
    void signUpThrowsExceptionWhenEmailExists() {
        // given
        when(userService.isAlreadyRegistered(anyString())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userAuthUseCase.signUp(signUpRequest))
                .isInstanceOf(RestApiException.class);

        verify(userService).isAlreadyRegistered("test@example.com");
        verify(userService, never()).save(any());
    }

    @Test
    @DisplayName("이미 존재하는 사용자 ID로 회원가입 시 예외를 던진다")
    void signUpThrowsExceptionWhenUserIdExists() {
        // given
        when(userService.isAlreadyRegistered(anyString())).thenReturn(false);
        when(userService.isUserIdAlreadyRegistered(anyString())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userAuthUseCase.signUp(signUpRequest))
                .isInstanceOf(RestApiException.class);

        verify(userService).isAlreadyRegistered("test@example.com");
        verify(userService).isUserIdAlreadyRegistered("testuser");
        verify(userService, never()).save(any());
    }

    @Test
    @DisplayName("로그인을 성공적으로 처리할 수 있다")
    void loginSuccess() {
        // given
        when(userService.findByUserId(anyString())).thenReturn(mockUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenProvider.createAccessToken(anyString())).thenReturn("accessToken123");
        when(tokenProvider.createRefreshToken(anyString())).thenReturn("refreshToken123");
        when(tokenProvider.getRemainingDuration(anyString())).thenReturn(java.util.Optional.of(Duration.ofDays(14)));

        // when
        LoginResponse result = userAuthUseCase.login(loginRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("accessToken123");
        assertThat(result.refreshToken()).isEqualTo("refreshToken123");
        verify(userService).findByUserId("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(tokenProvider).createAccessToken("testuser");
        verify(tokenProvider).createRefreshToken("testuser");
        verify(refreshTokenService).saveRefreshToken("testuser", "refreshToken123", Duration.ofDays(14));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 예외를 던진다")
    void loginThrowsExceptionWhenPasswordMismatch() {
        // given
        when(userService.findByUserId(anyString())).thenReturn(mockUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userAuthUseCase.login(loginRequest))
                .isInstanceOf(RestApiException.class);

        verify(userService).findByUserId("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(tokenProvider, never()).createAccessToken(anyString());
    }

    @Test
    @DisplayName("토큰 재발급을 성공적으로 처리할 수 있다")
    void reissueTokenSuccess() {
        // given
        when(tokenProvider.validateToken(anyString())).thenReturn(true);
        when(tokenProvider.getId(anyString())).thenReturn(java.util.Optional.of("testuser"));
        when(refreshTokenService.findByUserId(anyString())).thenReturn("refreshToken123");
        when(tokenProvider.getRemainingDuration(anyString())).thenReturn(java.util.Optional.of(Duration.ofDays(14)));
        when(tokenProvider.createAccessToken(anyString())).thenReturn("newAccessToken123");
        when(tokenProvider.createRefreshToken(anyString())).thenReturn("newRefreshToken123");

        // when
        TokenReissueResponse result = userAuthUseCase.reissueToken(tokenReissueRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("newAccessToken123");
        assertThat(result.refreshToken()).isEqualTo("newRefreshToken123");
        verify(tokenProvider).validateToken("refreshToken123");
        verify(tokenProvider).getId("refreshToken123");
        verify(refreshTokenService).findByUserId("testuser");
        verify(tokenProvider).createAccessToken("testuser");
        verify(tokenProvider).createRefreshToken("testuser");
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 재발급 시 예외를 던진다")
    void reissueTokenThrowsExceptionWhenInvalidRefreshToken() {
        // given
        when(tokenProvider.validateToken(anyString())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userAuthUseCase.reissueToken(tokenReissueRequest))
                .isInstanceOf(RestApiException.class);

        verify(tokenProvider).validateToken("refreshToken123");
        verify(tokenProvider, never()).getId(anyString());
    }
}
