package com.devmode.shop.global.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.devmode.shop.domain.user.domain.service.RefreshTokenService;
import com.devmode.shop.domain.user.domain.service.TokenWhitelistService;
import com.devmode.shop.global.config.properties.CorsProperties;
import com.devmode.shop.global.security.ExcludeAuthPathProperties;
import com.devmode.shop.global.security.JwtAuthenticationFilter;
import com.devmode.shop.global.security.TokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final TokenProvider tokenProvider;
	private final ExcludeAuthPathProperties excludeAuthPathProperties;
	private final RefreshTokenService refreshTokenService;
	private final TokenWhitelistService tokenWhitelistService;
	private final CorsProperties corsProperties;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http.cors(Customizer.withDefaults())
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable);

		http.authorizeHttpRequests(request -> {
			excludeAuthPathProperties.getPaths().iterator()
					.forEachRemaining(authPath ->
							request.requestMatchers(HttpMethod.valueOf(authPath.getMethod()), authPath.getPathPattern()).permitAll()
					);
		});

		http.authorizeHttpRequests(request -> request
				.requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
				.requestMatchers(
						"/swagger-ui/**",
						"/v3/api-docs/**",
						"/swagger-ui.html",
						"/webjars/**"
				).permitAll()
				.requestMatchers(
						"/actuator/**",
						"/favicon.ico"
				).permitAll()
				.requestMatchers(HttpMethod.POST, "/users/token").hasRole("USER") // 토큰 재발급
				// Authenticated
				.anyRequest().authenticated()
		);

		// Session 해제
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		// Jwt 커스텀 필터 등록
		http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

		// Token Exception Handling
		http.exceptionHandling(except -> except
				.authenticationEntryPoint((request, response, authException) -> response.sendError(response.getStatus(), "토큰 오류"))
		);

		return http.build();
	}

	@Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 HTTP 메서드
        config.setAllowedMethods(Arrays.asList(corsProperties.getAllowedMethods().split(",")));
        // 허용할 헤더
        config.setAllowedHeaders(Arrays.asList(corsProperties.getAllowedHeaders().split(",")));
        config.setAllowedOrigins(Arrays.asList(corsProperties.getAllowedOrigins().split(",")));
        // 인증정보(cookie, Authorization 헤더) 허용 여부
        config.setAllowCredentials(true);
        // pre-flight 캐시 시간 (초)
        config.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로에 대해 위 정책을 적용
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider, excludeAuthPathProperties, refreshTokenService, tokenWhitelistService);
    }
    
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

