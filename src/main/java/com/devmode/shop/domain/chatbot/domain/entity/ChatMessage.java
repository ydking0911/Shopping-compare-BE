package com.devmode.shop.domain.chatbot.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", unique = true, nullable = false, length = 100)
    private String messageId;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private MessageType type;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "intent", length = 50)
    private ChatIntent intent;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "timestamp", nullable = false)
    @CreatedDate
    private LocalDateTime timestamp;

    @Column(name = "is_processed", nullable = false)
    private boolean isProcessed = false;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Builder
    public ChatMessage(String messageId, String sessionId, String userId, 
                      MessageType type, String content, ChatIntent intent, String metadata) {
        this.messageId = messageId;
        this.sessionId = sessionId;
        this.userId = userId;
        this.type = type;
        this.content = content;
        this.intent = intent;
        this.metadata = metadata;
        this.timestamp = LocalDateTime.now();
    }

    public void markAsProcessed() {
        this.isProcessed = true;
    }

    public void setProcessingTime(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public void updateIntent(ChatIntent intent) {
        this.intent = intent;
    }

    public void updateMetadata(String metadata) {
        this.metadata = metadata;
    }
}
