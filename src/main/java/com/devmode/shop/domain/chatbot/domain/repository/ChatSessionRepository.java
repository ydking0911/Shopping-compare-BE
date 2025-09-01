package com.devmode.shop.domain.chatbot.domain.repository;

import com.devmode.shop.domain.chatbot.domain.entity.ChatSession;
import com.devmode.shop.domain.chatbot.domain.entity.ChatSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    Optional<ChatSession> findBySessionId(String sessionId);
    
    List<ChatSession> findByUserIdAndStatus(String userId, ChatSessionStatus status);
    
    List<ChatSession> findByStatusAndLastActivityAtBefore(ChatSessionStatus status, LocalDateTime before);
    
    @Query("SELECT cs FROM ChatSession cs WHERE cs.userId = :userId AND cs.status = 'ACTIVE' ORDER BY cs.lastActivityAt DESC")
    List<ChatSession> findActiveSessionsByUserId(@Param("userId") String userId);
    
    @Query("SELECT COUNT(cs) FROM ChatSession cs WHERE cs.status = 'ACTIVE' AND cs.userId = :userId")
    long countActiveSessionsByUserId(@Param("userId") String userId);
    
    boolean existsBySessionId(String sessionId);
    
    void deleteByStatusAndLastActivityAtBefore(ChatSessionStatus status, LocalDateTime before);
}
