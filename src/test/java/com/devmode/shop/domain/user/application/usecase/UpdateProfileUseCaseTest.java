package com.devmode.shop.domain.user.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.devmode.shop.domain.user.application.dto.request.UpdateProfileRequest;
import com.devmode.shop.domain.user.application.dto.response.ProfileResponse;
import com.devmode.shop.domain.user.domain.entity.User;
import com.devmode.shop.domain.user.domain.service.UserService;
import com.devmode.shop.global.exception.RestApiException;

@ExtendWith(MockitoExtension.class)
class UpdateProfileUseCaseTest {

	@Mock private UserService userService;
	@Mock private PasswordEncoder passwordEncoder;
	@InjectMocks private UpdateProfileUseCase useCase;

	@Test
	@DisplayName("프로필 수정: 비밀번호 미변경")
	void updateProfile_withoutPasswordChange() {
		User user = User.builder()
				.userId("testuser")
				.email("test@example.com")
				.password("encoded")
				.name("Tester")
				.birth("1990-01-01")
				.build();
		when(userService.findUser("testuser")).thenReturn(user);
		UpdateProfileRequest req = new UpdateProfileRequest("New Name", "test@example.com", "1990-01-01", null, null);

		ProfileResponse res = useCase.update("testuser", req);
		assertEquals("New Name", res.name());
	}

	@Test
	@DisplayName("프로필 수정: 비밀번호 변경 - 현재 비밀번호 틀림")
	void updateProfile_wrongCurrentPassword_throws() {
		User user = User.builder()
				.userId("testuser")
				.email("test@example.com")
				.password("encoded")
				.name("Tester")
				.birth("1990-01-01")
				.build();
		when(userService.findUser("testuser")).thenReturn(user);
		when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);
		UpdateProfileRequest req = new UpdateProfileRequest("New Name", "test@example.com", "1990-01-01", "wrong", "newpass");

		assertThrows(RestApiException.class, () -> useCase.update("testuser", req));
	}

	@Test
	@DisplayName("프로필 수정: 비밀번호 변경 - 성공")
	void updateProfile_changePassword_success() {
		User user = User.builder()
				.userId("testuser")
				.email("test@example.com")
				.password("encoded")
				.name("Tester")
				.birth("1990-01-01")
				.build();
		when(userService.findUser("testuser")).thenReturn(user);
		when(passwordEncoder.matches("current", "encoded")).thenReturn(true);
		when(passwordEncoder.encode("newpass")).thenReturn("encoded-new");
		UpdateProfileRequest req = new UpdateProfileRequest("New Name", "test@example.com", "1990-01-01", "current", "newpass");

		ProfileResponse res = useCase.update("testuser", req);
		assertEquals("New Name", res.name());
	}
}
