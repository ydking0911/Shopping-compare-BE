package com.devmode.shop.domain.product.domain.repository;

import com.devmode.shop.domain.product.domain.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    
    /**
     * 사용자 ID와 키워드로 검색 히스토리 조회
     */
    Optional<SearchHistory> findByUserIdAndKeyword(String userId, String keyword);
    
    /**
     * 사용자 ID로 검색 히스토리 목록 조회 (최신순)
     */
    List<SearchHistory> findByUserIdOrderByLastSearchedAtDesc(String userId);
    
    /**
     * 특정 키워드로 검색된 히스토리 목록 조회
     */
    List<SearchHistory> findByKeywordOrderByLastSearchedAtDesc(String keyword);
    
    /**
     * 사용자 ID로 검색 히스토리 개수 조회
     */
    long countByUserId(String userId);
    
    /**
     * 특정 기간 내 검색 히스토리 조회
     */
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.lastSearchedAt BETWEEN :startDate AND :endDate")
    List<SearchHistory> findBySearchDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * 사용자 ID와 특정 기간 내 검색 히스토리 조회
     */
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.userId = :userId AND sh.lastSearchedAt BETWEEN :startDate AND :endDate")
    List<SearchHistory> findByUserIdAndSearchDateBetween(@Param("userId") String userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * 캐시가 만료된 검색 히스토리 조회
     */
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.cacheExpiresAt < :currentTime")
    List<SearchHistory> findByExpiredCache(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 특정 사용자의 인기 검색 키워드 조회 (검색 횟수 기준)
     */
    @Query("SELECT sh.keyword, COUNT(sh) as searchCount FROM SearchHistory sh WHERE sh.userId = :userId GROUP BY sh.keyword ORDER BY searchCount DESC")
    List<Object[]> findPopularKeywordsByUserId(@Param("userId") String userId);
    
    /**
     * 전체 인기 검색 키워드 조회 (검색 횟수 기준)
     */
    @Query("SELECT sh.keyword, COUNT(sh) as searchCount FROM SearchHistory sh GROUP BY sh.keyword ORDER BY searchCount DESC")
    List<Object[]> findPopularKeywords();
    
    /**
     * 특정 사용자의 최근 검색 키워드 조회
     */
    @Query("SELECT DISTINCT sh.keyword FROM SearchHistory sh WHERE sh.userId = :userId ORDER BY sh.lastSearchedAt DESC")
    List<String> findRecentKeywordsByUserId(@Param("userId") String userId);
    
    /**
     * 캐시 키로 검색 히스토리 조회
     */
    Optional<SearchHistory> findByCacheKey(String cacheKey);
    
    /**
     * API 호출 횟수가 많은 검색 히스토리 조회
     */
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.apiCallCount > :threshold ORDER BY sh.apiCallCount DESC")
    List<SearchHistory> findByHighApiCallCount(@Param("threshold") int threshold);
    
    /**
     * 특정 사용자의 검색 히스토리 삭제
     */
    void deleteByUserId(String userId);
    
    /**
     * 특정 키워드의 검색 히스토리 삭제
     */
    void deleteByKeyword(String keyword);
    
    /**
     * 만료된 캐시를 가진 검색 히스토리 삭제
     */
    @Query("DELETE FROM SearchHistory sh WHERE sh.cacheExpiresAt < :currentTime")
    void deleteExpiredCache(@Param("currentTime") LocalDateTime currentTime);
}
