package com.devmode.shop.domain.user.ui;

import com.devmode.shop.global.annotation.AuthApi;
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
import com.devmode.shop.global.common.BaseResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class AuthController implements AuthApi {

	private final UserAuthUseCase userAuthUseCase;

	@PostMapping("/signup")
	public BaseResponse<?> signup(@Valid @RequestBody SignUpRequest request) {
		userAuthUseCase.signUp(request);
		return BaseResponse.onSuccess();
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return userAuthUseCase.login(request);
	}

	@DeleteMapping("/logout")
	@Override
	public BaseResponse<?> logout() {
		userAuthUseCase.logout(null); // HttpServletRequest는 필요에 따라 수정
		return BaseResponse.onSuccess();
	}
	
	@PostMapping("/reissue")
	@Override
	public TokenReissueResponse reissueToken(@Valid @RequestBody TokenReissueRequest request) {
		return userAuthUseCase.reissueToken(request);
	}
}