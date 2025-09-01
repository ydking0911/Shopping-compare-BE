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
@Table(name = "tool_calls")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ToolCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tool_call_id", unique = true, nullable = false, length = 100)
    private String toolCallId;

    @Column(name = "message_id", nullable = false, length = 100)
    private String messageId;

    @Column(name = "tool_name", nullable = false, length = 100)
    private String toolName;

    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters;

    @Column(name = "result", columnDefinition = "TEXT")
    private String result;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ToolCallStatus status;

    @Column(name = "executed_at")
    @CreatedDate
    private LocalDateTime executedAt;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Builder
    public ToolCall(String toolCallId, String messageId, String toolName, String parameters) {
        this.toolCallId = toolCallId;
        this.messageId = messageId;
        this.toolName = toolName;
        this.parameters = parameters;
        this.status = ToolCallStatus.PENDING;
        this.executedAt = LocalDateTime.now();
    }

    public void markAsSuccess(String result, long executionTimeMs) {
        this.status = ToolCallStatus.SUCCESS;
        this.result = result;
        this.executionTimeMs = executionTimeMs;
    }

    public void markAsFailed(String errorMessage) {
        this.status = ToolCallStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public boolean canRetry() {
        return this.retryCount < 3; // 최대 3번까지 재시도
    }
}
