package com.devmode.shop.domain.trend.domain.repository;

import com.devmode.shop.domain.trend.domain.entity.UserInterestKeywords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInterestKeywordsRepository extends JpaRepository<UserInterestKeywords, Long> {
    
    /**
     * 사용자 ID로 활성화된 관심 키워드 조회 (우선순위 순)
     */
    List<UserInterestKeywords> findByUserIdAndIsActiveTrueOrderByPriorityAsc(String userId);
    
    /**
     * 사용자 ID와 키워드로 관심 키워드 조회
     */
    Optional<UserInterestKeywords> findByUserIdAndKeywordAndIsActiveTrue(String userId, String keyword);
    
    /**
     * 사용자 ID로 관심 키워드 개수 조회
     */
    long countByUserIdAndIsActiveTrue(String userId);
    
    /**
     * 사용자 ID의 모든 관심 키워드 비활성화
     */
    @Query("UPDATE UserInterestKeywords uk SET uk.isActive = false WHERE uk.userId = :userId")
    void deactivateAllByUserId(@Param("userId") String userId);
    
    /**
     * 사용자 ID로 관심 키워드 문자열 목록 조회
     */
    @Query("SELECT uk.keyword FROM UserInterestKeywords uk WHERE uk.userId = :userId AND uk.isActive = true ORDER BY uk.priority ASC")
    List<String> findKeywordsByUserId(@Param("userId") String userId);
}
