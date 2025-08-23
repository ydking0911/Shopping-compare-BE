package com.devmode.shop.domain.user.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User 엔티티 테스트")
class UserTest {

    @Test
    @DisplayName("User 엔티티 생성 테스트")
    void createUser() {
        // given
        String userId = "user123";
        String name = "홍길동";
        String email = "hong@example.com";
        String password = "encodedPassword123";
        String birth = "1990-01-01";

        // when
        User user = User.builder()
                .userId(userId)
                .name(name)
                .email(email)
                .password(password)
                .birth(birth)
                .build();

        // then
        assertThat(user.getUserId()).isEqualTo(userId);
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getBirth()).isEqualTo(birth);
    }

    @Test
    @DisplayName("User 엔티티 기본 생성자 테스트")
    void createUserWithNoArgsConstructor() {
        // when
        User user = new User();

        // then
        assertThat(user).isNotNull();
        assertThat(user.getUserId()).isNull();
        assertThat(user.getName()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getPassword()).isNull();
        assertThat(user.getBirth()).isNull();
    }

    @Test
    @DisplayName("User 엔티티 AllArgsConstructor 테스트")
    void createUserWithAllArgsConstructor() {
        // given
        String userId = "user456";
        String name = "김철수";
        String email = "kim@example.com";
        String password = "encodedPassword456";
        String birth = "1995-05-15";

        // when
        User user = new User(userId, name, email, password, birth);

        // then
        assertThat(user.getUserId()).isEqualTo(userId);
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getBirth()).isEqualTo(birth);
    }

    @Test
    @DisplayName("User 프로필 업데이트 테스트 - 모든 필드 변경")
    void updateProfile_AllFields() {
        // given
        User user = User.builder()
                .userId("user789")
                .name("이영희")
                .email("lee@example.com")
                .password("oldPassword")
                .birth("1988-12-25")
                .build();

        String newName = "이미영";
        String newBirth = "1988-06-15";
        String newPassword = "newEncodedPassword";

        // when
        user.updateProfile(newName, newBirth, newPassword);

        // then
        assertThat(user.getName()).isEqualTo(newName);
        assertThat(user.getBirth()).isEqualTo(newBirth);
        assertThat(user.getPassword()).isEqualTo(newPassword);
        assertThat(user.getUserId()).isEqualTo("user789"); // 변경되지 않음
        assertThat(user.getEmail()).isEqualTo("lee@example.com"); // 변경되지 않음
    }

    @Test
    @DisplayName("User 프로필 업데이트 테스트 - 이름과 생년월일만 변경")
    void updateProfile_NameAndBirthOnly() {
        // given
        User user = User.builder()
                .userId("user101")
                .name("박민수")
                .email("park@example.com")
                .password("currentPassword")
                .birth("1992-03-10")
                .build();

        String newName = "박준호";
        String newBirth = "1992-08-20";
        String originalPassword = user.getPassword();

        // when
        user.updateProfile(newName, newBirth, null);

        // then
        assertThat(user.getName()).isEqualTo(newName);
        assertThat(user.getBirth()).isEqualTo(newBirth);
        assertThat(user.getPassword()).isEqualTo(originalPassword); // 변경되지 않음
    }

    @Test
    @DisplayName("User 프로필 업데이트 테스트 - 빈 비밀번호로 변경 시도")
    void updateProfile_EmptyPassword() {
        // given
        User user = User.builder()
                .userId("user202")
                .name("최수진")
                .email("choi@example.com")
                .password("originalPassword")
                .birth("1993-07-22")
                .build();

        String newName = "최미영";
        String newBirth = "1993-11-05";
        String emptyPassword = "   "; // 공백만 있는 비밀번호
        String originalPassword = user.getPassword();

        // when
        user.updateProfile(newName, newBirth, emptyPassword);

        // then
        assertThat(user.getName()).isEqualTo(newName);
        assertThat(user.getBirth()).isEqualTo(newBirth);
        assertThat(user.getPassword()).isEqualTo(originalPassword); // 빈 문자열이므로 변경되지 않음
    }

    @Test
    @DisplayName("User 프로필 업데이트 테스트 - null 비밀번호로 변경 시도")
    void updateProfile_NullPassword() {
        // given
        User user = User.builder()
                .userId("user303")
                .name("정현우")
                .email("jung@example.com")
                .password("originalPassword")
                .birth("1991-09-14")
                .build();

        String newName = "정민수";
        String newBirth = "1991-04-30";
        String originalPassword = user.getPassword();

        // when
        user.updateProfile(newName, newBirth, null);

        // then
        assertThat(user.getName()).isEqualTo(newName);
        assertThat(user.getBirth()).isEqualTo(newBirth);
        assertThat(user.getPassword()).isEqualTo(originalPassword); // null이므로 변경되지 않음
    }

    @Test
    @DisplayName("User 엔티티 동등성 테스트")
    void userEquality() {
        // given
        User user1 = User.builder()
                .userId("user404")
                .name("한지민")
                .email("han@example.com")
                .password("password123")
                .birth("1994-02-18")
                .build();

        User user2 = User.builder()
                .userId("user404") // 동일한 userId
                .name("한지민")
                .email("han@example.com")
                .password("password123")
                .birth("1994-02-18")
                .build();

        User user3 = User.builder()
                .userId("user505") // 다른 userId
                .name("한지민")
                .email("han@example.com")
                .password("password123")
                .birth("1994-02-18")
                .build();

        // then
        // 일반 클래스는 equals/hashCode가 기본 구현되어 있으므로 참조 비교
        assertThat(user1).isNotEqualTo(user2); // 다른 객체 참조
        assertThat(user1).isNotEqualTo(user3); // 다른 객체 참조
        // userId로 내용 비교
        assertThat(user1.getUserId()).isEqualTo(user2.getUserId());
        assertThat(user1.getUserId()).isNotEqualTo(user3.getUserId());
    }
}
