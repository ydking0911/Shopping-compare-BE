package com.devmode.shop.domain.user.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devmode.shop.domain.user.application.dto.response.ProfileResponse;
import com.devmode.shop.domain.user.domain.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserProfileUseCaseTest {

	@Mock private UserService userService;
	@InjectMocks private UserProfileUseCase useCase;

	@Test
	@DisplayName("프로필 조회 성공")
	void findProfile_success() {
		ProfileResponse expected = new ProfileResponse("testuser", "test@example.com", "Tester", "1990-01-01");
		when(userService.findProfile("testuser")).thenReturn(expected);

		ProfileResponse result = useCase.findProfile("testuser");
		assertEquals("testuser", result.userId());
		assertEquals("test@example.com", result.email());
		assertEquals("Tester", result.name());
		assertEquals("1990-01-01", result.birth());
	}
}
