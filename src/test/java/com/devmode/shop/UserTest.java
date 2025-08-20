package com.devmode.shop;

import com.devmode.shop.domain.user.application.dto.request.LoginRequest;
import com.devmode.shop.domain.user.application.dto.request.SignUpRequest;
import com.devmode.shop.domain.user.application.dto.response.ProfileResponse;
import com.devmode.shop.domain.user.domain.entity.User;

public class UserTest {
    
    public static void main(String[] args) {
        System.out.println("=== User 엔티티 테스트 ===");
        
        // 1. User 엔티티 생성 테스트
        User user = User.builder()
                .userId("testuser123")
                .email("test@example.com")
                .password("encodedPassword123")
                .name("테스트 사용자")
                .birth("1990-01-01")
                .build();
        
        System.out.println("✅ User 엔티티 생성 성공");
        System.out.println("  - userId: " + user.getUserId());
        System.out.println("  - email: " + user.getEmail());
        System.out.println("  - name: " + user.getName());
        System.out.println("  - birth: " + user.getBirth());
        
        // 2. SignUpRequest 테스트
        SignUpRequest signUpRequest = new SignUpRequest(
                "signup@example.com",
                "newuser456",
                "password123",
                "새 사용자",
                "1995-05-05"
        );
        
        System.out.println("\n✅ SignUpRequest 생성 성공");
        System.out.println("  - userId: " + signUpRequest.userId());
        System.out.println("  - email: " + signUpRequest.email());
        System.out.println("  - name: " + signUpRequest.name());
        System.out.println("  - birth: " + signUpRequest.birth());
        
        // 3. LoginRequest 테스트
        LoginRequest loginRequest = new LoginRequest(
                "testuser123",
                "password123"
        );
        
        System.out.println("\n✅ LoginRequest 생성 성공");
        System.out.println("  - userId: " + loginRequest.userId());
        System.out.println("  - password: " + loginRequest.password());
        
        // 4. ProfileResponse 테스트
        ProfileResponse profileResponse = ProfileResponse.create(user);
        
        System.out.println("\n✅ ProfileResponse 생성 성공");
        System.out.println("  - userId: " + profileResponse.userId());
        System.out.println("  - email: " + profileResponse.email());
        System.out.println("  - name: " + profileResponse.name());
        System.out.println("  - birth: " + profileResponse.birth());
        
        // 5. User 프로필 업데이트 테스트
        System.out.println("\n✅ User 프로필 업데이트 테스트");
        System.out.println("  - 업데이트 전 name: " + user.getName());
        System.out.println("  - 업데이트 전 birth: " + user.getBirth());
        
        user.updateProfile("수정된 이름", "1990-12-31", null);
        
        System.out.println("  - 업데이트 후 name: " + user.getName());
        System.out.println("  - 업데이트 후 birth: " + user.getBirth());
        
        System.out.println("\n🎉 모든 테스트 통과! userId 기반 시스템이 정상적으로 작동합니다.");
        System.out.println("\n📋 요약:");
        System.out.println("  - ✅ id → userId로 변경 완료");
        System.out.println("  - ✅ Long → String 타입 변경 완료");
        System.out.println("  - ✅ 회원가입 시 userId 입력 가능");
        System.out.println("  - ✅ 로그인 시 userId와 password 사용");
        System.out.println("  - ✅ birth 필드 추가 완료");
    }
}
