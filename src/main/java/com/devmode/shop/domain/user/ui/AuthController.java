package com.devmode.shop.domain.user.ui;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devmode.shop.domain.user.application.dto.request.LoginRequest;
import com.devmode.shop.domain.user.application.dto.request.SignUpRequest;
import com.devmode.shop.domain.user.application.dto.response.LoginResponse;
import com.devmode.shop.domain.user.application.usecase.UserAuthUseCase;
import com.devmode.shop.global.common.BaseResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class AuthController {

	private final UserAuthUseCase userAuthUseCase;

	/**
	 *  회원가입
	 */

	@PostMapping("/sign-up")
	public BaseResponse<Void> signUp(@Valid @RequestBody SignUpRequest request) {
		userAuthUseCase.signUp(request);
		return BaseResponse.onSuccess();
	}


	@PostMapping("/login")
	public BaseResponse<LoginResponse> login(
			@Valid
			@RequestBody
			LoginRequest request) {
		return BaseResponse.onSuccess(userAuthUseCase.login(request));
	}

	@DeleteMapping("/logout")
    public BaseResponse<Void> logout(HttpServletRequest request) {
        userAuthUseCase.logout(request);
        return BaseResponse.onSuccess();
    }
}