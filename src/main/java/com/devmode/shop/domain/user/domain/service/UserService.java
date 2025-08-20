package com.devmode.shop.domain.user.domain.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devmode.shop.domain.user.application.dto.request.SignUpRequest;
import com.devmode.shop.domain.user.application.dto.response.ProfileResponse;
import com.devmode.shop.domain.user.domain.entity.User;
import com.devmode.shop.domain.user.domain.repository.UserRepository;
import com.devmode.shop.global.exception.RestApiException;
import static com.devmode.shop.global.exception.code.status.GlobalErrorStatus._NOT_FOUND;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public User findByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new RestApiException(_NOT_FOUND));
	}
	
	public User findByUserId(String userId) {
		return userRepository.findByUserId(userId)
				.orElseThrow(() -> new RestApiException(_NOT_FOUND));
	}

	public boolean isAlreadyRegistered(String email) {
		return userRepository.existsByEmail(email);
	}
	
	public boolean isUserIdAlreadyRegistered(String userId) {
		return userRepository.existsByUserId(userId);
	}

	@Transactional
	public User save(SignUpRequest request, String code) {
		User user = User.builder()
				.userId(request.userId())
				.email(request.email())
				.password(passwordEncoder.encode(request.password()))
				.name(request.name())
				.birth(request.birth())
				.build();
		return userRepository.save(user);
	}

	public User findUser(String userId) {
		return userRepository.findByUserId(userId)
				.orElseThrow(() -> new RestApiException(_NOT_FOUND));
	}

	public ProfileResponse findProfile(String userId) {
		User user = userRepository.findByUserId(userId)
				.orElseThrow(() -> new RestApiException(_NOT_FOUND));
		return ProfileResponse.create(user);
	}
}