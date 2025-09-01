package com.devmode.shop.domain.user.ui;

import com.devmode.shop.domain.user.application.dto.request.UpdateProfileRequest;
import com.devmode.shop.domain.user.application.dto.response.ProfileResponse;
import com.devmode.shop.domain.user.application.usecase.UpdateProfileUseCase;
import com.devmode.shop.domain.user.application.usecase.UserProfileUseCase;
import com.devmode.shop.global.test.BaseControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController 테스트")
class UserControllerTest extends BaseControllerTest<UserController> {

    @Mock
    private UserProfileUseCase userProfileUseCase;

    @Mock
    private UpdateProfileUseCase updateProfileUseCase;

    @InjectMocks
    private UserController userController;

    private ProfileResponse profileResponse;
    private UpdateProfileRequest updateProfileRequest;

    @Override
    protected UserController getController() {
        return userController;
    }

    @BeforeEach
    void setUp() {

        // ProfileResponse 필드 순서: userId, email, name, birth
        profileResponse = new ProfileResponse(
                "user123",
                "hong@example.com",
                "홍길동",
                "1990-01-01"
        );

        updateProfileRequest = new UpdateProfileRequest(
                "김철수",
                "kim@example.com",
                "1992-05-15",
                "currentPassword123",
                "newPassword123"
        );
    }

    @Test
    @DisplayName("GET /api/users/profile - 프로필 조회 성공 테스트")
    void getProfile_Success() throws Exception {
        // given
        String userId = "user123";
        testResolver.setTestUserId(userId);
        when(userProfileUseCase.findProfile(anyString())).thenReturn(profileResponse);

        // when & then
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.userId").value("user123"))
                .andExpect(jsonPath("$.result.email").value("hong@example.com"))
                .andExpect(jsonPath("$.result.name").value("홍길동"))
                .andExpect(jsonPath("$.result.birth").value("1990-01-01"));

        verify(userProfileUseCase, times(1)).findProfile(userId);
    }

    @Test
    @DisplayName("PATCH /api/users/profile - 프로필 수정 성공 테스트")
    void updateProfile_Success() throws Exception {
        // given
        String userId = "user123";
        testResolver.setTestUserId(userId);
        ProfileResponse updatedProfile = new ProfileResponse(
                "user123",
                "kim@example.com",
                "김철수",
                "1992-05-15"
        );
        when(updateProfileUseCase.update(anyString(), any(UpdateProfileRequest.class)))
                .thenReturn(updatedProfile);

        String updateRequestJson = objectMapper.writeValueAsString(updateProfileRequest);

        // when & then
        mockMvc.perform(patch("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.userId").value("user123"))
                .andExpect(jsonPath("$.result.email").value("kim@example.com"))
                .andExpect(jsonPath("$.result.name").value("김철수"))
                .andExpect(jsonPath("$.result.birth").value("1992-05-15"));

        verify(updateProfileUseCase, times(1)).update(eq(userId), any(UpdateProfileRequest.class));
    }

    @Test
    @DisplayName("PATCH /api/users/profile - 유효성 검증 실패 테스트")
    void updateProfile_ValidationFailed() throws Exception {
        // given
        String userId = "user123";
        testResolver.setTestUserId(userId);
        String invalidUpdateRequestJson = """
                {
                    "name": "",
                    "email": "",
                    "birth": "invalid-date",
                    "currentPassword": "123",
                    "newPassword": "123"
                }
                """;

        // when & then
        mockMvc.perform(patch("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUpdateRequestJson))
                .andExpect(status().isBadRequest());

        verify(updateProfileUseCase, never()).update(anyString(), any(UpdateProfileRequest.class));
    }

    @Test
    @DisplayName("PATCH /api/users/profile - 비밀번호 없이 프로필 수정 테스트")
    void updateProfile_WithoutPassword() throws Exception {
        // given
        String userId = "user123";
        testResolver.setTestUserId(userId);
        UpdateProfileRequest requestWithoutPassword = new UpdateProfileRequest(
                "김철수",
                "kim@example.com",
                "1992-05-15",
                null,
                null
        );
        ProfileResponse updatedProfile = new ProfileResponse(
                "user123",
                "kim@example.com",
                "김철수",
                "1992-05-15"
        );
        when(updateProfileUseCase.update(anyString(), any(UpdateProfileRequest.class)))
                .thenReturn(updatedProfile);

        String updateRequestJson = objectMapper.writeValueAsString(requestWithoutPassword);

        // when & then
        mockMvc.perform(patch("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.email").value("kim@example.com"))
                .andExpect(jsonPath("$.result.name").value("김철수"))
                .andExpect(jsonPath("$.result.birth").value("1992-05-15"));

        verify(updateProfileUseCase, times(1)).update(eq(userId), any(UpdateProfileRequest.class));
    }

    @Test
    @DisplayName("다른 사용자 ID로 테스트")
    void testWithDifferentUserId() throws Exception {
        // given
        String differentUserId = "user456";
        testResolver.setTestUserId(differentUserId);
        when(userProfileUseCase.findProfile(anyString())).thenReturn(profileResponse);

        // when & then
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk());

        verify(userProfileUseCase, times(1)).findProfile(differentUserId);
    }

    @Test
    @DisplayName("빈 문자열 userId로 테스트")
    void testWithEmptyUserId() throws Exception {
        // given
        testResolver.setTestUserId("");

        // when & then
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk()); // 빈 문자열도 유효한 userId로 처리

        verify(userProfileUseCase, times(1)).findProfile("");
    }

    @Test
    @DisplayName("@CurrentUser 어노테이션 시뮬레이션 검증")
    void testCurrentUserAnnotationSimulation() throws Exception {
        // given
        String testUserId = "testUser789";
        testResolver.setTestUserId(testUserId);
        when(userProfileUseCase.findProfile(anyString())).thenReturn(profileResponse);

        // when & then
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk());

        // @CurrentUser 어노테이션으로 전달된 userId가 올바르게 UseCase에 전달되는지 확인
        verify(userProfileUseCase, times(1)).findProfile(testUserId);
    }

    @Test
    @DisplayName("@CurrentUser 어노테이션으로 다양한 userId 전달 테스트")
    void testCurrentUserWithVariousUserIds() throws Exception {
        // given
        String[] testUserIds = {"user001", "user002", "user003", "admin123", "guest456"};
        
        for (String userId : testUserIds) {
            testResolver.setTestUserId(userId);
            when(userProfileUseCase.findProfile(anyString())).thenReturn(profileResponse);

            // when & then
            mockMvc.perform(get("/api/users/profile"))
                    .andExpect(status().isOk());

            // @CurrentUser 어노테이션으로 전달된 userId가 올바르게 UseCase에 전달되는지 확인
            verify(userProfileUseCase, times(1)).findProfile(userId);
            
            // 다음 테스트를 위해 mock 초기화
            reset(userProfileUseCase);
        }
    }

    @Test
    @DisplayName("@CurrentUser 어노테이션으로 프로필 수정 시 userId 전달 테스트")
    void testCurrentUserInProfileUpdate() throws Exception {
        // given
        String userId = "updateUser123";
        testResolver.setTestUserId(userId);
        ProfileResponse updatedProfile = new ProfileResponse(
                userId,
                "updated@example.com",
                "업데이트된이름",
                "1995-12-25"
        );
        when(updateProfileUseCase.update(anyString(), any(UpdateProfileRequest.class)))
                .thenReturn(updatedProfile);

        String updateRequestJson = objectMapper.writeValueAsString(updateProfileRequest);

        // when & then
        mockMvc.perform(patch("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestJson))
                .andExpect(status().isOk());

        // @CurrentUser 어노테이션으로 전달된 userId가 올바르게 UseCase에 전달되는지 확인
        verify(updateProfileUseCase, times(1)).update(eq(userId), any(UpdateProfileRequest.class));
    }
}
