package com.devmode.shop.domain.user.ui;

import com.devmode.shop.global.swagger.AuthApi;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devmode.shop.domain.user.application.dto.request.LoginRequest;
import com.devmode.shop.domain.user.application.dto.request.SignUpRequest;
import com.devmode.shop.domain.user.application.dto.request.TokenReissueRequest;
import com.devmode.shop.domain.user.application.dto.response.LoginResponse;
import com.devmode.shop.domain.user.application.dto.response.TokenReissueResponse;
import com.devmode.shop.domain.user.application.usecase.UserAuthUseCase;
import com.devmode.shop.global.annotation.CurrentUser;
import com.devmode.shop.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class AuthController implements AuthApi {

	private final UserAuthUseCase userAuthUseCase;

	@PostMapping("/signup")
	public BaseResponse<Void> signup(@Valid @RequestBody SignUpRequest request) {
		userAuthUseCase.signUp(request);
		return BaseResponse.onSuccess();
	}

	@PostMapping("/login")
	public BaseResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		return BaseResponse.onSuccess(userAuthUseCase.login(request));
	}

	@DeleteMapping("/logout")
	@Override
	public BaseResponse<Void> logout(HttpServletRequest request) {
		userAuthUseCase.logout(request);
		return BaseResponse.onSuccess();
	}

	/**
	 * 사용자 ID로 로그아웃 (@CurrentUser 사용)
	 */
	@DeleteMapping("/logout/user")
	public BaseResponse<Void> logoutByUserId(@Parameter(hidden = true) @CurrentUser String userId) {
		userAuthUseCase.logout(userId);
		return BaseResponse.onSuccess();
	}
	
	@PostMapping("/reissue")
	@Override
	public BaseResponse<TokenReissueResponse> reissueToken(@Valid @RequestBody TokenReissueRequest request) {
		return BaseResponse.onSuccess(userAuthUseCase.reissueToken(request));
	}
}