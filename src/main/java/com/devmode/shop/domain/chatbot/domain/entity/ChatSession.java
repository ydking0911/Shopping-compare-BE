package com.devmode.shop.domain.chatbot.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", unique = true, nullable = false, length = 100)
    private String sessionId;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChatSessionStatus status;

    @Column(name = "context", columnDefinition = "TEXT")
    private String context;

    @Column(name = "started_at", nullable = false)
    @CreatedDate
    private LocalDateTime startedAt;

    @Column(name = "last_activity_at", nullable = false)
    @LastModifiedDate
    private LocalDateTime lastActivityAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Builder
    public ChatSession(String sessionId, String userId, String context) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.context = context;
        this.status = ChatSessionStatus.ACTIVE;
        this.startedAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(24); // 24시간 후 만료
    }

    public void updateLastActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    public void close() {
        this.status = ChatSessionStatus.CLOSED;
        this.lastActivityAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = ChatSessionStatus.EXPIRED;
        this.lastActivityAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == ChatSessionStatus.ACTIVE && 
               LocalDateTime.now().isBefore(this.expiresAt);
    }

    public void extendExpiration() {
        this.expiresAt = LocalDateTime.now().plusHours(24);
    }
}
