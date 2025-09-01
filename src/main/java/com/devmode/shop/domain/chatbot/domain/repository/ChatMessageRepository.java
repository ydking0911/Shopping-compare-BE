package com.devmode.shop.domain.chatbot.domain.repository;

import com.devmode.shop.domain.chatbot.domain.entity.ChatMessage;
import com.devmode.shop.domain.chatbot.domain.entity.ChatIntent;
import com.devmode.shop.domain.chatbot.domain.entity.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByTimestampAsc(String sessionId);
    
    List<ChatMessage> findByUserIdOrderByTimestampDesc(String userId);
    
    Page<ChatMessage> findByUserIdAndType(String userId, MessageType type, Pageable pageable);
    
    List<ChatMessage> findBySessionIdAndTypeOrderByTimestampAsc(String sessionId, MessageType type);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.sessionId = :sessionId AND cm.timestamp >= :since ORDER BY cm.timestamp ASC")
    List<ChatMessage> findRecentMessagesBySessionId(@Param("sessionId") String sessionId, @Param("since") LocalDateTime since);
    
    @Query("SELECT cm.intent, COUNT(cm) FROM ChatMessage cm WHERE cm.userId = :userId AND cm.intent IS NOT NULL GROUP BY cm.intent ORDER BY COUNT(cm) DESC")
    List<Object[]> findIntentDistributionByUserId(@Param("userId") String userId);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.userId = :userId AND cm.intent = :intent ORDER BY cm.timestamp DESC")
    List<ChatMessage> findByUserIdAndIntent(@Param("userId") String userId, @Param("intent") ChatIntent intent);
    
    long countBySessionId(String sessionId);
    
    long countByUserIdAndType(String userId, MessageType type);
    
    @Query("SELECT AVG(cm.processingTimeMs) FROM ChatMessage cm WHERE cm.isProcessed = true AND cm.processingTimeMs IS NOT NULL")
    Double getAverageProcessingTime();
}
