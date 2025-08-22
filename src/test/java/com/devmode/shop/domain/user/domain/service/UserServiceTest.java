package com.devmode.shop.domain.user.domain.service;

import com.devmode.shop.domain.user.application.dto.request.SignUpRequest;
import com.devmode.shop.domain.user.application.dto.response.ProfileResponse;
import com.devmode.shop.domain.user.domain.entity.User;
import com.devmode.shop.domain.user.domain.repository.UserRepository;
import com.devmode.shop.global.exception.RestApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private SignUpRequest signUpRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId("user123")
                .name("홍길동")
                .email("hong@example.com")
                .password("encodedPassword123")
                .birth("1990-01-01")
                .build();

        signUpRequest = new SignUpRequest(
                "user123",
                "hong@example.com",
                "password123",
                "홍길동",
                "1990-01-01"
        );
    }

    @Test
    @DisplayName("이메일로 사용자 조회 성공 테스트")
    void findByEmail_Success() {
        // given
        String email = "hong@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // when
        User result = userService.findByEmail(email);

        // then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("이메일로 사용자 조회 실패 테스트 - 사용자 없음")
    void findByEmail_NotFound() {
        // given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findByEmail(email))
                .isInstanceOf(RestApiException.class);

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("사용자 ID로 사용자 조회 성공 테스트")
    void findByUserId_Success() {
        // given
        String userId = "user123";
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(testUser));

        // when
        User result = userService.findByUserId(userId);

        // then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("사용자 ID로 사용자 조회 실패 테스트 - 사용자 없음")
    void findByUserId_NotFound() {
        // given
        String userId = "nonexistent";
        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findByUserId(userId))
                .isInstanceOf(RestApiException.class);

        verify(userRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("이메일 중복 확인 테스트 - 이미 등록됨")
    void isAlreadyRegistered_True() {
        // given
        String email = "hong@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // when
        boolean result = userService.isAlreadyRegistered(email);

        // then
        assertThat(result).isTrue();
        verify(userRepository, times(1)).existsByEmail(email);
    }

    @Test
    @DisplayName("이메일 중복 확인 테스트 - 등록되지 않음")
    void isAlreadyRegistered_False() {
        // given
        String email = "new@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // when
        boolean result = userService.isAlreadyRegistered(email);

        // then
        assertThat(result).isFalse();
        verify(userRepository, times(1)).existsByEmail(email);
    }

    @Test
    @DisplayName("사용자 ID 중복 확인 테스트 - 이미 등록됨")
    void isUserIdAlreadyRegistered_True() {
        // given
        String userId = "user123";
        when(userRepository.existsByUserId(userId)).thenReturn(true);

        // when
        boolean result = userService.isUserIdAlreadyRegistered(userId);

        // then
        assertThat(result).isTrue();
        verify(userRepository, times(1)).existsByUserId(userId);
    }

    @Test
    @DisplayName("사용자 ID 중복 확인 테스트 - 등록되지 않음")
    void isUserIdAlreadyRegistered_False() {
        // given
        String userId = "newuser";
        when(userRepository.existsByUserId(userId)).thenReturn(false);

        // when
        boolean result = userService.isUserIdAlreadyRegistered(userId);

        // then
        assertThat(result).isFalse();
        verify(userRepository, times(1)).existsByUserId(userId);
    }

    @Test
    @DisplayName("사용자 등록 성공 테스트")
    void save_Success() {
        // given
        String encodedPassword = "encodedPassword123";
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // when
        User result = userService.save(signUpRequest);

        // then
        assertThat(result).isEqualTo(testUser);
        verify(passwordEncoder, times(1)).encode(signUpRequest.password());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 등록 시 비밀번호 인코딩 테스트")
    void save_PasswordEncoding() {
        // given
        String encodedPassword = "encodedPassword123";
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // when
        userService.save(signUpRequest);

        // then
        verify(passwordEncoder, times(1)).encode(signUpRequest.password());
        // User 객체가 올바른 인코딩된 비밀번호로 생성되었는지 확인
        verify(userRepository, times(1)).save(argThat(user -> 
                user.getPassword().equals(encodedPassword) &&
                user.getUserId().equals(signUpRequest.userId()) &&
                user.getEmail().equals(signUpRequest.email()) &&
                user.getName().equals(signUpRequest.name()) &&
                user.getBirth().equals(signUpRequest.birth())
        ));
    }

    @Test
    @DisplayName("사용자 프로필 조회 성공 테스트")
    void findProfile_Success() {
        // given
        String userId = "user123";
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(testUser));

        // when
        ProfileResponse result = userService.findProfile(userId);

        // then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("사용자 프로필 조회 실패 테스트 - 사용자 없음")
    void findProfile_NotFound() {
        // given
        String userId = "nonexistent";
        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findProfile(userId))
                .isInstanceOf(RestApiException.class);

        verify(userRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("사용자 조회 성공 테스트")
    void findUser_Success() {
        // given
        String userId = "user123";
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(testUser));

        // when
        User result = userService.findUser(userId);

        // then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("사용자 조회 실패 테스트 - 사용자 없음")
    void findUser_NotFound() {
        // given
        String userId = "nonexistent";
        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findUser(userId))
                .isInstanceOf(RestApiException.class);

        verify(userRepository, times(1)).findByUserId(userId);
    }
}
