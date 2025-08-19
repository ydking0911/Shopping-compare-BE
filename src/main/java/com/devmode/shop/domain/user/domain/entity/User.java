package com.devmode.shop.domain.user.domain.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String name;

	@Column
	private String birth;

	@Column(nullable = false)
	private String role = "ROLE_USER";

	@Column(nullable = false)
	private OffsetDateTime createdAt = OffsetDateTime.now();

	@Column
	private OffsetDateTime lastLoginAt;

	@Builder
	public User(String email, String password, String name, String birth) {
		this.email = email;
		this.password = password;
		this.name = name;
		this.birth = birth;
	}

	public void updateRole(String role) {
		this.role = role;
	}

	public void updateProfile(String name, String birth, String encodedNewPassword) {
		this.name = name;
		this.birth = birth;
		if (encodedNewPassword != null && !encodedNewPassword.isBlank()) {
			this.password = encodedNewPassword;
		}
	}

	public void updateLastLoginAt(OffsetDateTime when) {
		this.lastLoginAt = when;
	}
}


