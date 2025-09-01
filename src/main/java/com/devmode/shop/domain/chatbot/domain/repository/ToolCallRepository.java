package com.devmode.shop.domain.chatbot.domain.repository;

import com.devmode.shop.domain.chatbot.domain.entity.ToolCall;
import com.devmode.shop.domain.chatbot.domain.entity.ToolCallStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ToolCallRepository extends JpaRepository<ToolCall, Long> {

    List<ToolCall> findByMessageId(String messageId);
    
    List<ToolCall> findByToolName(String toolName);
    
    List<ToolCall> findByStatus(ToolCallStatus status);
    
    List<ToolCall> findByStatusAndExecutedAtBefore(ToolCallStatus status, LocalDateTime before);
    
    @Query("SELECT tc FROM ToolCall tc WHERE tc.status = 'FAILED' AND tc.retryCount < 3 ORDER BY tc.executedAt ASC")
    List<ToolCall> findFailedToolCallsForRetry();
    
    @Query("SELECT tc.toolName, COUNT(tc), AVG(tc.executionTimeMs) FROM ToolCall tc WHERE tc.status = 'SUCCESS' GROUP BY tc.toolName")
    List<Object[]> findToolCallStatistics();
    
    @Query("SELECT tc FROM ToolCall tc WHERE tc.messageId = :messageId AND tc.status = 'SUCCESS' ORDER BY tc.executedAt DESC")
    List<ToolCall> findSuccessfulToolCallsByMessageId(@Param("messageId") String messageId);
    
    @Query("SELECT COUNT(tc) FROM ToolCall tc WHERE tc.status = :status AND tc.executedAt >= :since")
    long countByStatusAndExecutedAtAfter(@Param("status") ToolCallStatus status, @Param("since") LocalDateTime since);
    
    @Query("SELECT AVG(tc.executionTimeMs) FROM ToolCall tc WHERE tc.status = 'SUCCESS' AND tc.executionTimeMs IS NOT NULL")
    Double getAverageExecutionTime();
    
    @Query("SELECT tc FROM ToolCall tc WHERE tc.status = 'PENDING' AND tc.executedAt < :timeout ORDER BY tc.executedAt ASC")
    List<ToolCall> findPendingToolCallsWithTimeout(@Param("timeout") LocalDateTime timeout);
}
