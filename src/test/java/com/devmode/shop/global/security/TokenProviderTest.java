package com.devmode.shop.global.security;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TokenProviderTest {

	@Test
	@DisplayName("액세스 토큰 생성 및 파싱")
	void createAndParseAccessToken() {
		JwtProperties props = new JwtProperties();
		props.setKey("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
		props.setAccessTokenExpirationPeriodDay(86400000L); // 1일을 밀리초로
		props.setRefreshTokenExpirationPeriodDay(1209600000L); // 14일을 밀리초로

		TokenProvider provider = new TokenProvider(props);
		String token = provider.createAccessToken("testuser");
		assertNotNull(token);
		assertTrue(provider.validateToken(token));
		assertEquals("testuser", provider.getId(token).orElse(null));
		assertTrue(provider.getRemainingDuration(token).orElse(Duration.ZERO).toMillis() > 0);
	}
}
